package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import com.alfgoto.rune.visuals.menu.MenuBackground;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The Runes page of the Rune Codex: the 8 Player Rune slots on top with Capacity used / max, then the
 * Player Runes found in the player's inventory. Clicking an inventory Rune installs it (the item is
 * consumed); clicking an installed Rune removes it (the item goes back to the inventory, or drops).
 * It only calls {@link PlayerLoadout}, so the Capacity and slot rules are the ones of the Loadout.
 */
final class RunesMenu extends Menu {

    static final int INSTALLED_FIRST_SLOT = 0;
    static final int CAPACITY_SLOT = 8;
    static final int INVENTORY_FIRST_SLOT = 18;
    static final int INVENTORY_LAST_SLOT = 44;

    private final PlayerLoadout loadout;
    private final RuneRegistry runes;
    private final Player player;

    RunesMenu(Player player, Menu parent, PlayerLoadout loadout, RuneRegistry runes) {
        super(MenuBackground.RUNES.title(Text.translatable("rune.menu.runes.title", "Runes")), 6, parent);
        this.player = player;
        this.loadout = loadout;
        this.runes = runes;
        draw();
    }

    private void draw() {
        clear();
        UUID id = player.getUniqueId();
        List<ToolLoadout.Installed> installed = loadout.installed(id);
        for (int i = 0; i < PlayerLoadout.SLOTS; i++) {
            int slot = i + 1;
            set(
                    INSTALLED_FIRST_SLOT + i,
                    i < installed.size() ? installedButton(installed.get(i), slot) : emptySlot(slot));
        }
        set(CAPACITY_SLOT, capacityButton(id));

        int next = INVENTORY_FIRST_SLOT;
        for (Stored stored : inventoryRunes().values()) {
            if (next > INVENTORY_LAST_SLOT) {
                break; // more kinds than room: the rest show once some are installed
            }
            set(next++, inventoryButton(stored));
        }
    }

    private Button emptySlot(int slot) {
        return Button.of(
                Material.GRAY_STAINED_GLASS_PANE,
                Text.translatable("rune.menu.runes.empty_slot", "Empty slot %s", Component.text(slot))
                        .color(NamedTextColor.DARK_GRAY),
                p -> {});
    }

    private Button installedButton(ToolLoadout.Installed entry, int slot) {
        ItemStack icon = withHint(
                runes.create(entry.rune(), shownRank(entry.rune(), entry.rank())),
                Text.translatable("rune.menu.runes.click_remove", "Click to remove").color(NamedTextColor.YELLOW));
        return new Button(icon, p -> remove(slot));
    }

    /** A stored Rank above the Max Rank (changed in code later) is shown as the Max Rank. */
    private static int shownRank(Rune rune, int rank) {
        return rune.maxRank().isPresent() ? Math.min(rank, rune.maxRank().getAsInt()) : rank;
    }

    private Button inventoryButton(Stored stored) {
        ItemStack icon = runes.create(stored.rune(), shownRank(stored.rune(), stored.rank()));
        icon.setAmount(Math.min(stored.count(), 64));
        withHint(
                icon,
                Text.translatable("rune.menu.runes.click_install", "Click to install").color(NamedTextColor.YELLOW));
        return new Button(icon, p -> install(stored.rune(), stored.rank()));
    }

    private Button capacityButton(UUID id) {
        long used = loadout.used(id);
        int max = loadout.capacity(id);
        String model = used > max ? "capacity_over" : used == max ? "capacity_full" : "capacity_free";
        Button button = Button.of(
                Material.PAPER,
                Text.translatable(
                                "rune.menu.runes.capacity",
                                "Capacity: %s / %s",
                                Component.text(used),
                                Component.text(max))
                        .color(NamedTextColor.GREEN),
                p -> {});
        button.icon().editMeta(meta -> meta.setItemModel(new NamespacedKey(Text.NAMESPACE, model)));
        return button;
    }

    private void install(Rune rune, int rank) {
        int slot = findInInventory(rune, rank);
        if (slot < 0) {
            draw(); // the stack is gone: just redraw
            return;
        }
        UUID id = player.getUniqueId();
        ToolLoadout.Result result = loadout.install(id, rune, rank);
        if (result == ToolLoadout.Result.INSTALLED) {
            ItemStack stack = player.getInventory().getItem(slot);
            int left = stack.getAmount() - 1;
            if (left <= 0) {
                player.getInventory().setItem(slot, null);
            } else {
                stack.setAmount(left);
                player.getInventory().setItem(slot, stack);
            }
        } else {
            player.sendMessage(failure(result, rune, rank, id));
        }
        draw();
    }

    private void remove(int slot) {
        loadout.uninstall(player.getUniqueId(), slot).ifPresent(removed -> {
            ItemStack stack = runes.create(removed.rune(), shownRank(removed.rune(), removed.rank()));
            player.getInventory()
                    .addItem(stack)
                    .values()
                    .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
        });
        draw();
    }

    private Component failure(ToolLoadout.Result result, Rune rune, int rank, UUID id) {
        return switch (result) {
            case NO_FREE_SLOT -> error(
                    "rune.menu.runes.no_slot",
                    "All %s Player Rune slots are used.",
                    Component.text(PlayerLoadout.SLOTS));
            case OVER_CAPACITY -> error(
                    "rune.menu.runes.over_capacity",
                    "Not enough Capacity: this Rune costs %s, %s left.",
                    Component.text(runes.capacityCost(rune, rank)),
                    Component.text(Math.max(0, loadout.capacity(id) - loadout.used(id))));
            default -> error("rune.menu.runes.cannot", "This Rune can't be installed on the player.");
        };
    }

    private int findInInventory(Rune rune, int rank) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null
                    && runes.of(contents[i]).filter(r -> r == rune).isPresent()
                    && runes.rankOf(contents[i]) == rank) {
                return i;
            }
        }
        return -1;
    }

    /** The Player Runes in the inventory, one entry per Rune and Rank, with how many there are. */
    private Map<String, Stored> inventoryRunes() {
        Map<String, Stored> found = new LinkedHashMap<>();
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null) {
                continue;
            }
            runes.of(stack).filter(rune -> rune.fits().contains(Fit.PLAYER)).ifPresent(rune -> {
                int rank = runes.rankOf(stack);
                found.merge(
                        rune.id() + ":" + rank,
                        new Stored(rune, rank, stack.getAmount()),
                        (a, b) -> new Stored(a.rune(), a.rank(), a.count() + b.count()));
            });
        }
        return found;
    }

    private static ItemStack withHint(ItemStack icon, Component hint) {
        icon.editMeta(meta -> {
            List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            lore.add(Component.empty());
            lore.add(hint.decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        return icon;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Text.translatable(key, fallback, args).color(NamedTextColor.RED);
    }

    private record Stored(Rune rune, int rank, int count) {}
}
