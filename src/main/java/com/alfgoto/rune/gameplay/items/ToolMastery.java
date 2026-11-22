package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.config.ToolConfig;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Mastery (docs/jobs/mining.md, woodcutting.md): a Rune Tool gains +1 Capacity every N valuable Job
 * Actions, up to a share of its base Capacity. The count is kept on the stack in its PDC under
 * {@code rune:mastery}, so it travels with the tool. Pickaxes count ores, axes count logs; other
 * tools have no Mastery until their Job is designed.
 */
public final class ToolMastery {

    private final ItemRegistry items;
    private final ToolConfig config;
    private final NamespacedKey key;

    public ToolMastery(Plugin plugin, ItemRegistry items, ToolConfig config) {
        this.items = items;
        this.config = config;
        this.key = new NamespacedKey(plugin, "mastery");
    }

    /** Actions needed for +1 Capacity on this kind of tool, or 0 when the kind has no Mastery. */
    public int actionsPerCapacity(ToolKind kind) {
        return switch (kind) {
            case PICKAXE -> config.pickaxeMasteryActions();
            case AXE -> config.axeMasteryActions();
            case SHOVEL -> 0;
        };
    }

    /** Whether breaking {@code block} is a valuable Job Action for a tool of {@code kind}. */
    public static boolean counts(ToolKind kind, Material block) {
        return switch (kind) {
            case PICKAXE -> isOre(block);
            case AXE -> isLog(block);
            case SHOVEL -> false;
        };
    }

    /** Ores: the vanilla ore tags, nether gold, nether quartz and ancient debris. */
    static boolean isOre(Material block) {
        return Tag.COAL_ORES.isTagged(block)
                || Tag.COPPER_ORES.isTagged(block)
                || Tag.DIAMOND_ORES.isTagged(block)
                || Tag.EMERALD_ORES.isTagged(block)
                || Tag.GOLD_ORES.isTagged(block)
                || Tag.IRON_ORES.isTagged(block)
                || Tag.LAPIS_ORES.isTagged(block)
                || Tag.REDSTONE_ORES.isTagged(block)
                || block == Material.NETHER_GOLD_ORE
                || block == Material.NETHER_QUARTZ_ORE
                || block == Material.ANCIENT_DEBRIS;
    }

    /** Logs: logs, wood, Nether stems, giant mushroom stems, chorus plants. Leaves do not count. */
    public static boolean isLog(Material block) {
        return Tag.LOGS.isTagged(block) || block == Material.MUSHROOM_STEM || block == Material.CHORUS_PLANT;
    }

    /** The Capacity Mastery has added at {@code progress}: floor(progress / perCapacity), capped. */
    public static int bonus(double progress, int perCapacity, int maxBonus) {
        if (perCapacity <= 0 || progress <= 0) {
            return 0;
        }
        return (int) Math.min(maxBonus, Math.floor(progress / perCapacity + 1e-9));
    }

    /** The most Capacity Mastery can add to a tool with this base Capacity (share of it, rounded down). */
    public int maxBonus(int baseCapacity) {
        return (int) Math.floor(baseCapacity * config.masteryMaxBonus() + 1e-9);
    }

    /** Mastery of {@code stack}, or empty when it is not a Rune Tool with Mastery. */
    public Optional<MasteryStatus> status(ItemStack stack) {
        Optional<RuneItem> item = items.of(stack);
        if (item.isEmpty()) {
            return Optional.empty();
        }
        return items.toolStats(item.get()).flatMap(stats -> {
            int per = actionsPerCapacity(stats.kind());
            if (per <= 0) {
                return Optional.empty();
            }
            double progress = progress(stack);
            int max = maxBonus(stats.capacity());
            return Optional.of(new MasteryStatus(progress, per, stats.capacity(), bonus(progress, per, max), max));
        });
    }

    /** Actions counted on {@code stack} (0 if none). */
    public double progress(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return 0;
        }
        Double value = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        return value == null ? 0 : value;
    }

    /**
     * Counts {@code amount} more actions on {@code stack} and redraws its lore. Stops counting once
     * Mastery is maxed. Does nothing when the stack has no Mastery. Returns whether it counted.
     */
    public boolean add(ItemStack stack, double amount) {
        if (!(amount > 0)) {
            return false;
        }
        Optional<MasteryStatus> status = status(stack);
        if (status.isEmpty() || status.get().maxed()) {
            return false;
        }
        double next = status.get().progress() + amount;
        items.update(stack, meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, next));
        return true;
    }
}
