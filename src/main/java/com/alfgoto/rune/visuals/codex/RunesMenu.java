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
import com.alfgoto.rune.visuals.menu.MenuCanvas;
import com.alfgoto.rune.visuals.menu.MenuCanvas.Align;
import com.alfgoto.rune.visuals.menu.MenuFont;
import com.alfgoto.rune.visuals.menu.MenuImage;
import com.alfgoto.rune.visuals.menu.MenuStrings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The Runes page of the Rune Codex, an altar: the 8 Player Rune sockets in a ring around the Capacity orb
 * (it fills with Capacity used / max), then a satchel with the Player Runes found in the player's inventory.
 * Clicking a satchel Rune installs it (the item is consumed); clicking an installed Rune removes it (the item
 * goes back to the inventory, or drops). It only calls {@link PlayerLoadout}, so the Capacity and slot rules
 * are the ones of the Loadout. Layout positions match {@code tools/texture-gen/v3/menus.py} ("Runes").
 */
final class RunesMenu extends Menu {

    /** The 8 Player Rune sockets, in slot order (clockwise from the top left). */
    static final int[] SOCKETS = {3, 5, 15, 24, 32, 30, 20, 11};

    /** The orb: every slot of it shows the Capacity. */
    static final int[] ORB_SLOTS = {12, 13, 14, 21, 22, 23};

    static final int CAPACITY_SLOT = 13;
    static final int INVENTORY_FIRST_SLOT = 36;
    static final int INVENTORY_LAST_SLOT = 53;
    static final int BACK_SLOT = 27;
    static final int PREVIOUS_SLOT = 26;
    static final int NEXT_SLOT = 35;

    private static final int PAGE_SIZE = INVENTORY_LAST_SLOT - INVENTORY_FIRST_SLOT + 1;
    private static final int ORB_X = 88 - 15;
    private static final TextColor TITLE = TextColor.color(225, 200, 255);

    private final PlayerLoadout loadout;
    private final RuneRegistry runes;
    private final Player player;
    private int page;

    RunesMenu(Player player, Menu parent, PlayerLoadout loadout, RuneRegistry runes) {
        super(Component.empty(), MenuBackground.ROWS, parent);
        this.player = player;
        this.loadout = loadout;
        this.runes = runes;
        draw();
    }

    @Override
    protected int backSlot() {
        return BACK_SLOT;
    }

    @Override
    protected Button backButton() {
        return Button.invisible(Text.translatable("rune.menu.back", "Back").color(NamedTextColor.YELLOW), List.of(),
                this::back);
    }

    private void draw() {
        clear();
        UUID id = player.getUniqueId();
        List<ToolLoadout.Installed> installed = loadout.installed(id);
        for (int i = 0; i < PlayerLoadout.SLOTS && i < SOCKETS.length; i++) {
            int slot = i + 1;
            set(SOCKETS[i], i < installed.size() ? installedButton(installed.get(i), slot) : emptySlot(slot));
        }
        long used = loadout.used(id);
        int max = loadout.capacity(id);
        Button capacity = capacityButton(id);
        for (int slot : ORB_SLOTS) {
            set(slot, capacity);
        }
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.RUNES)
                .text(88, 6, MenuStrings.of(player).get("rune.menu.runes.title", "Runes"), TITLE, MenuFont.SHADOW,
                        Align.CENTER, 120)
                .image(MenuImage.orb(used, max), ORB_X)
                .text(88, 48, used + "/" + max, NamedTextColor.WHITE, MenuFont.SHADOW, Align.CENTER, 29);

        List<Stored> stored = new ArrayList<>(inventoryRunes().values());
        int pages = Math.max(1, (stored.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));
        for (int i = 0; i < PAGE_SIZE && page * PAGE_SIZE + i < stored.size(); i++) {
            set(INVENTORY_FIRST_SLOT + i, inventoryButton(stored.get(page * PAGE_SIZE + i)));
        }
        if (page > 0) {
            canvas.image(MenuImage.RUNES_PREVIOUS, 7 + 18 * (PREVIOUS_SLOT % 9));
            set(PREVIOUS_SLOT, Button.invisible(Text.translatable("rune.menu.previous", "Previous page")
                    .color(NamedTextColor.YELLOW), List.of(), p -> {
                        page--;
                        draw();
                    }));
        }
        if (page < pages - 1) {
            canvas.image(MenuImage.RUNES_NEXT, 7 + 18 * (NEXT_SLOT % 9));
            set(NEXT_SLOT, Button.invisible(Text.translatable("rune.menu.next", "Next page")
                    .color(NamedTextColor.YELLOW), List.of(), p -> {
                        page++;
                        draw();
                    }));
        }
        retitle(canvas.build());
    }

    private Button emptySlot(int slot) {
        return Button.withModel(
                Button.EMPTY,
                Text.translatable("rune.menu.runes.empty_slot", "Empty slot %s", Component.text(slot))
                        .color(NamedTextColor.DARK_GRAY),
                List.of(),
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

    /** Invisible: the orb drawn under it shows the Capacity; the tooltip gives the numbers. */
    private Button capacityButton(UUID id) {
        long used = loadout.used(id);
        int max = loadout.capacity(id);
        return Button.invisible(
                Text.translatable("rune.menu.runes.capacity", "Capacity: %s / %s", Component.text(used),
                                Component.text(max))
                        .color(used > max ? NamedTextColor.RED : NamedTextColor.GREEN),
                List.of(),
                p -> {});
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
