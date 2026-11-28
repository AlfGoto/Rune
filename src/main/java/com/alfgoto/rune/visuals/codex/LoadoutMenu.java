package com.alfgoto.rune.visuals.codex;

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
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A Loadout menu, drawn as an altar: the Rune sockets in a ring around the Capacity orb (it fills with Capacity
 * used / max), then a satchel with the Runes found in the player's inventory that fit. Clicking a satchel Rune
 * installs it (the item is consumed); clicking an installed Rune removes it (the item goes back to the
 * inventory, or drops). A subclass gives the Loadout (the player's, or a piece of gear), so the Capacity and
 * slot rules are the ones of that Loadout. Layout positions match {@code tools/texture-gen/v3/menus.py}
 * ("Runes"). A subclass calls {@link #draw()} at the end of its constructor.
 */
abstract class LoadoutMenu extends Menu {

    /** The 8 sockets, in slot order (clockwise from the top left). */
    static final int[] SOCKETS = {3, 5, 15, 24, 32, 30, 20, 11};

    /** The orb: every slot of it shows the Capacity. */
    static final int[] ORB_SLOTS = {12, 13, 14, 21, 22, 23};

    static final int CAPACITY_SLOT = 13;
    static final int INVENTORY_FIRST_SLOT = 36;
    static final int INVENTORY_LAST_SLOT = 53;
    static final int BACK_SLOT = 27;
    static final int PREVIOUS_SLOT = 26;
    static final int NEXT_SLOT = 35;

    private static final NamespacedKey LOCKED = new NamespacedKey("rune", "locked_slot");
    private static final int PAGE_SIZE = INVENTORY_LAST_SLOT - INVENTORY_FIRST_SLOT + 1;
    private static final int ORB_X = 88 - 15;
    private static final TextColor TITLE = TextColor.color(225, 200, 255);

    protected final RuneRegistry runes;
    protected final Player player;
    private int page;

    LoadoutMenu(Player player, Menu parent, RuneRegistry runes) {
        super(Component.empty(), MenuBackground.ROWS, parent);
        this.player = player;
        this.runes = runes;
    }

    /** The installed Runes, in slot order. */
    protected abstract List<ToolLoadout.Installed> installed();

    protected abstract long used();

    protected abstract int capacity();

    /** How many Rune slots this Loadout has (at most {@link #SOCKETS}). */
    protected abstract int slotCount();

    protected abstract ToolLoadout.Result installRune(Rune rune, int rank);

    /** Removes the Rune in {@code slot} (from 1). */
    protected abstract Optional<ToolLoadout.Installed> uninstallRune(int slot);

    /** Whether {@code rune} can go in this Loadout (only those are listed in the satchel). */
    protected abstract boolean fits(Rune rune);

    protected abstract String title(MenuStrings text);

    protected abstract Component noSlotMessage();

    protected abstract Component cannotMessage();

    /** False when what this menu edits is gone: the menu then closes. */
    protected boolean stillValid() {
        return true;
    }

    /** A short mark drawn on the socket of {@code index} (from 0), or null for a plain slot. */
    protected String marker(int index) {
        return null;
    }

    /** Extra tooltip lines of the slot {@code index} (from 0). */
    protected List<Component> slotNotes(int index) {
        return List.of();
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

    protected final void draw() {
        if (!stillValid()) {
            player.closeInventory();
            return;
        }
        clear();
        List<ToolLoadout.Installed> installed = installed();
        int slots = Math.min(slotCount(), SOCKETS.length);
        for (int i = 0; i < SOCKETS.length; i++) {
            int slot = i + 1;
            if (i >= slots) {
                set(SOCKETS[i], lockedSlot());
            } else {
                set(SOCKETS[i], i < installed.size() ? installedButton(installed.get(i), slot) : emptySlot(slot));
            }
        }
        long used = used();
        int max = capacity();
        Button capacity = capacityButton();
        for (int slot : ORB_SLOTS) {
            set(slot, capacity);
        }
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.RUNES)
                .text(88, 6, title(MenuStrings.of(player)), TITLE, MenuFont.SHADOW, Align.CENTER, 120)
                .image(MenuImage.orb(used, max), ORB_X)
                .text(88, 48, used + "/" + max, NamedTextColor.WHITE, MenuFont.SHADOW, Align.CENTER, 29);
        for (int i = 0; i < slots; i++) {
            String mark = marker(i);
            if (mark != null) {
                int socket = SOCKETS[i];
                canvas.text(7 + 18 * (socket % 9) + 2, 18 + 18 * (socket / 9) + 2, mark, NamedTextColor.GOLD,
                        MenuFont.SHADOW, Align.LEFT);
            }
        }

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
                slotNotes(slot - 1),
                p -> {});
    }

    /** A slot this Loadout does not have (a piece of gear has 1 to 5 of the 8 sockets). */
    private Button lockedSlot() {
        return Button.withModel(
                LOCKED,
                Text.translatable("rune.menu.gear.locked_slot", "No slot").color(NamedTextColor.DARK_GRAY),
                List.of(),
                p -> {});
    }

    private Button installedButton(ToolLoadout.Installed entry, int slot) {
        ItemStack icon = runes.create(entry.rune(), shownRank(entry.rune(), entry.rank()));
        List<Component> notes = slotNotes(slot - 1);
        if (!notes.isEmpty()) {
            icon.editMeta(meta -> {
                List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
                lore.add(Component.empty());
                notes.forEach(note -> lore.add(note.decoration(TextDecoration.ITALIC, false)));
                meta.lore(lore);
            });
        }
        withHint(icon, Text.translatable("rune.menu.runes.click_remove", "Click to remove")
                .color(NamedTextColor.YELLOW));
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
    private Button capacityButton() {
        long used = used();
        int max = capacity();
        return Button.invisible(
                Text.translatable("rune.menu.runes.capacity", "Capacity: %s / %s", Component.text(used),
                                Component.text(max))
                        .color(used > max ? NamedTextColor.RED : NamedTextColor.GREEN),
                List.of(),
                p -> {});
    }

    private void install(Rune rune, int rank) {
        if (!stillValid()) {
            draw();
            return;
        }
        int slot = findInInventory(rune, rank);
        if (slot < 0) {
            draw(); // the stack is gone: just redraw
            return;
        }
        ToolLoadout.Result result = installRune(rune, rank);
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
            player.sendMessage(failure(result, rune, rank));
        }
        draw();
    }

    private void remove(int slot) {
        if (stillValid()) {
            uninstallRune(slot).ifPresent(removed -> {
                ItemStack stack = runes.create(removed.rune(), shownRank(removed.rune(), removed.rank()));
                player.getInventory()
                        .addItem(stack)
                        .values()
                        .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            });
        }
        draw();
    }

    private Component failure(ToolLoadout.Result result, Rune rune, int rank) {
        return switch (result) {
            case NO_FREE_SLOT -> noSlotMessage();
            case OVER_CAPACITY -> error(
                    "rune.menu.runes.over_capacity",
                    "Not enough Capacity: this Rune costs %s, %s left.",
                    Component.text(runes.capacityCost(rune, rank)),
                    Component.text(Math.max(0, capacity() - used())));
            default -> cannotMessage();
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

    /** The fitting Runes in the inventory, one entry per Rune and Rank, with how many there are. */
    private Map<String, Stored> inventoryRunes() {
        Map<String, Stored> found = new LinkedHashMap<>();
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack == null) {
                continue;
            }
            runes.of(stack).filter(this::fits).ifPresent(rune -> {
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

    protected static Component error(String key, String fallback, Component... args) {
        return Text.translatable(key, fallback, args).color(NamedTextColor.RED);
    }

    private record Stored(Rune rune, int rank, int count) {}
}
