package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ArmorKind;
import com.alfgoto.rune.gameplay.items.ArmorStats;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolStats;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * The Loadout of a Rune Tool or a Rune Armor piece: which Runes are installed, the Capacity check and
 * the effect totals. In the method names, "tool" means either one.
 *
 * <p>The installed Runes are stored on the item's PersistentDataContainer under {@code rune:loadout}, as
 * {@code id:rank;id:rank}, so they travel with it. Slots are the positions in that list, from 1;
 * uninstalling a Rune moves the next ones up. Installing and uninstalling are free and possible anytime.
 *
 * <p>Capacity is the base Capacity ({@link ToolStats#capacity()} or {@link ArmorStats#capacity()}).
 * Mastery (Rune Tools only) and Traits that change Capacity are not built yet.
 *
 * <p>Rune Armor takes the passive Runes that fit the player (Armor Runes), or that list its own piece
 * ({@link Fit#HELMET}…) as a Fit.
 */
public final class ToolLoadout {

    /** One installed Rune with its Rank. */
    public record Installed(Rune rune, int rank) {}

    /** The result of {@link #install}. */
    public enum Result {
        INSTALLED,
        /** The stack is not a Rune Tool or Rune Armor. */
        NOT_A_TOOL,
        /** The Rune cannot go in this kind of tool or armor piece. */
        DOES_NOT_FIT,
        /** The Rank is not allowed by the Rune's Max Rank (or is below 1). */
        BAD_RANK,
        /** Every Rune slot of the tool is used. */
        NO_FREE_SLOT,
        /** The Capacity Cost would go over the tool's Capacity. */
        OVER_CAPACITY
    }

    private final ItemRegistry items;
    private final RuneRegistry runes;
    private final NamespacedKey loadoutKey;

    public ToolLoadout(Plugin plugin, ItemRegistry items, RuneRegistry runes) {
        this.items = items;
        this.runes = runes;
        this.loadoutKey = new NamespacedKey(plugin, "loadout");
    }

    /** The {@link Fit} of a kind of tool. */
    public static Fit fitOf(ToolKind kind) {
        return switch (kind) {
            case PICKAXE -> Fit.PICKAXE;
            case AXE -> Fit.AXE;
            case SHOVEL -> Fit.SHOVEL;
        };
    }

    /** The {@link Fit} of a kind of armor piece. */
    public static Fit fitOf(ArmorKind kind) {
        return switch (kind) {
            case HELMET -> Fit.HELMET;
            case CHESTPLATE -> Fit.CHESTPLATE;
            case LEGGINGS -> Fit.LEGGINGS;
            case BOOTS -> Fit.BOOTS;
        };
    }

    /** The final stats of {@code tool} if it is a Rune Tool. */
    public Optional<ToolStats> stats(ItemStack tool) {
        return items.of(tool).flatMap(items::toolStats);
    }

    /** The final stats of {@code armor} if it is a Rune Armor piece. */
    public Optional<ArmorStats> armorStats(ItemStack armor) {
        return items.of(armor).flatMap(items::armorStats);
    }

    /** What a Loadout allows on one item: slots, Capacity and which Runes fit. */
    private record Limits(int slots, int capacity, Predicate<Rune> fits) {}

    private Optional<Limits> limits(ItemStack stack) {
        Optional<ToolStats> tool = stats(stack);
        if (tool.isPresent()) {
            ToolStats s = tool.get();
            Fit fit = fitOf(s.kind());
            return Optional.of(new Limits(s.slots(), s.capacity(), rune -> rune.fits().contains(fit)));
        }
        return armorStats(stack).map(s -> {
            Fit fit = fitOf(s.kind());
            return new Limits(
                    s.slots(),
                    s.capacity(),
                    rune -> rune.fits().contains(Fit.PLAYER) || rune.fits().contains(fit));
        });
    }

    /** The installed Runes, in slot order. Empty for anything that is not a Rune Tool or Rune Armor. */
    public List<Installed> installed(ItemStack tool) {
        List<Installed> list = new ArrayList<>();
        if (limits(tool).isEmpty()) {
            return list;
        }
        String raw = tool.getItemMeta().getPersistentDataContainer().get(loadoutKey, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) {
            return list;
        }
        for (String entry : raw.split(";")) {
            int colon = entry.lastIndexOf(':');
            if (colon < 1) {
                continue;
            }
            Optional<Rune> rune = runes.find(entry.substring(0, colon));
            if (rune.isEmpty()) {
                continue; // a Rune that no longer exists
            }
            try {
                list.add(new Installed(rune.get(), Math.max(1, Integer.parseInt(entry.substring(colon + 1)))));
            } catch (NumberFormatException e) {
                // a damaged entry is skipped
            }
        }
        return list;
    }

    /** The Capacity used by the installed Runes. */
    public long used(ItemStack tool) {
        long sum = 0;
        for (Installed installed : installed(tool)) {
            sum += runes.capacityCost(installed.rune(), installed.rank());
        }
        return sum;
    }

    /** The Capacity of the tool or armor piece, or 0 if it is neither. */
    public int capacity(ItemStack tool) {
        return limits(tool).map(Limits::capacity).orElse(0);
    }

    /** Installs {@code rune} at {@code rank} on {@code tool}. The item is changed only when it succeeds. */
    public Result install(ItemStack tool, Rune rune, int rank) {
        Optional<Limits> limits = limits(tool);
        if (limits.isEmpty()) {
            return Result.NOT_A_TOOL;
        }
        if (!limits.get().fits().test(rune)) {
            return Result.DOES_NOT_FIT;
        }
        if (!rune.allowsRank(rank)) {
            return Result.BAD_RANK;
        }
        List<Installed> list = installed(tool);
        if (list.size() >= limits.get().slots()) {
            return Result.NO_FREE_SLOT;
        }
        if (used(tool) + runes.capacityCost(rune, rank) > limits.get().capacity()) {
            return Result.OVER_CAPACITY;
        }
        list.add(new Installed(rune, rank));
        write(tool, list);
        return Result.INSTALLED;
    }

    /**
     * Removes the Rune in {@code slot} (from 1) and returns it, or empty if the slot is empty or the stack is
     * not a Rune Tool.
     */
    public Optional<Installed> uninstall(ItemStack tool, int slot) {
        List<Installed> list = installed(tool);
        if (slot < 1 || slot > list.size()) {
            return Optional.empty();
        }
        Installed removed = list.remove(slot - 1);
        write(tool, list);
        return Optional.of(removed);
    }

    /**
     * Carries the Runes of {@code sources} over to {@code result} (an Upgrade): each Rune is installed in order
     * (sources in the given order, slots in order) if it still fits, and the Runes that do not fit are returned
     * so the caller can give them back. Sources that are not Rune Tools are ignored.
     */
    public List<Installed> inherit(ItemStack result, List<ItemStack> sources) {
        List<Installed> overflow = new ArrayList<>();
        for (ItemStack source : sources) {
            for (Installed old : installed(source)) {
                if (install(result, old.rune(), old.rank()) != Result.INSTALLED) {
                    overflow.add(old);
                }
            }
        }
        return overflow;
    }

    /**
     * Every installed Rune as an effect source, ready for {@link Stacking}. This is what feeds the Effect
     * system. The tool's Trait is not built yet, so it is not in the list.
     */
    public List<Stacking.Source> sources(ItemStack tool) {
        List<Stacking.Source> sources = new ArrayList<>();
        for (Installed installed : installed(tool)) {
            sources.add(new Stacking.Source(installed.rune().effect(), installed.rank()));
        }
        return sources;
    }

    /** The stacked value of the effect {@code effectId} on {@code tool} (see {@link Stacking#total}). */
    public double total(ItemStack tool, String effectId) {
        return Stacking.total(effectId, sources(tool));
    }

    private void write(ItemStack tool, List<Installed> list) {
        StringBuilder raw = new StringBuilder();
        for (Installed installed : list) {
            if (!raw.isEmpty()) {
                raw.append(';');
            }
            raw.append(installed.rune().id()).append(':').append(installed.rank());
        }
        String value = raw.toString();
        items.update(tool, meta -> {
            if (value.isEmpty()) {
                meta.getPersistentDataContainer().remove(loadoutKey);
            } else {
                meta.getPersistentDataContainer().set(loadoutKey, PersistentDataType.STRING, value);
            }
        });
    }
}
