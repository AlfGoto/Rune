package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.foundations.triggers.ClickType;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolStats;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.MenuStrings;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The Loadout of a Rune Tool or Rune Armor piece, opened by right-clicking it in the inventory: the same altar as
 * the Runes page, with that item's slots and Capacity, and only the Runes that fit it. It edits the item that is
 * in the clicked inventory slot, so the change stays on the item (also when it is worn). When that item leaves
 * the slot, the menu closes on the next click. The Active Slots of a tool are marked with a letter (EN "R" / "L",
 * FR "D" / "G") for right-click / left-click in air and say so in their tooltip.
 */
final class GearMenu extends LoadoutMenu {

    private final ToolLoadout loadout;
    private final ItemRegistry items;
    private final int inventorySlot;
    private final String itemId;

    /** {@code inventorySlot} is the index in the player's inventory (36 to 39 worn armor, 40 offhand). */
    GearMenu(Player player, RuneRegistry runes, ToolLoadout loadout, ItemRegistry items, int inventorySlot) {
        super(player, null, runes);
        this.loadout = loadout;
        this.items = items;
        this.inventorySlot = inventorySlot;
        this.itemId = items.of(gear()).map(RuneItem::id).orElse("");
        draw();
    }

    /** What is in the slot now (an empty stack when nothing is). */
    private ItemStack gear() {
        ItemStack stack = player.getInventory().getItem(inventorySlot);
        return stack == null ? ItemStack.empty() : stack;
    }

    @Override
    protected boolean stillValid() {
        return !itemId.isEmpty()
                && items.of(gear()).map(RuneItem::id).filter(itemId::equals).isPresent();
    }

    /** Runs {@code change} on the item in the slot and puts it back, so the change stays on the item. */
    private <T> T edit(Function<ItemStack, T> change) {
        ItemStack stack = gear();
        T result = change.apply(stack);
        player.getInventory().setItem(inventorySlot, stack);
        return result;
    }

    @Override
    protected List<ToolLoadout.Installed> installed() {
        return loadout.installed(gear());
    }

    @Override
    protected long used() {
        return loadout.used(gear());
    }

    @Override
    protected int capacity() {
        return loadout.capacity(gear());
    }

    @Override
    protected int slotCount() {
        return loadout.slots(gear());
    }

    @Override
    protected ToolLoadout.Result installRune(Rune rune, int rank) {
        return edit(stack -> loadout.install(stack, rune, rank));
    }

    @Override
    protected Optional<ToolLoadout.Installed> uninstallRune(int slot) {
        return edit(stack -> loadout.uninstall(stack, slot));
    }

    @Override
    protected boolean fits(Rune rune) {
        return loadout.fits(gear(), rune);
    }

    @Override
    protected String title(MenuStrings text) {
        return text.get("rune.menu.gear.title", "Gear Loadout");
    }

    @Override
    protected Component noSlotMessage() {
        return error("rune.menu.gear.no_slot", "All %s Rune slots of this item are used.",
                Component.text(loadout.slots(gear())));
    }

    @Override
    protected Component cannotMessage() {
        return error("rune.menu.gear.cannot", "This Rune doesn't fit this item.");
    }

    /** The click of the Active Slot {@code index} (from 0), if this is a tool and the slot is an Active Slot. */
    private Optional<ClickType> activeClick(int index) {
        List<ClickType> clicks =
                loadout.stats(gear()).map(ToolStats::kind).map(k -> k.activeClicks()).orElse(List.of());
        return index < clicks.size() ? Optional.of(clicks.get(index)) : Optional.empty();
    }

    @Override
    protected String marker(int index) {
        MenuStrings text = MenuStrings.of(player);
        return activeClick(index).map(click -> click == ClickType.RIGHT
                ? text.get("rune.menu.gear.mark_right", "R")
                : text.get("rune.menu.gear.mark_left", "L")).orElse(null);
    }

    @Override
    protected List<Component> slotNotes(int index) {
        Optional<ClickType> click = activeClick(index);
        if (click.isEmpty()) {
            return List.of();
        }
        Component note = click.get() == ClickType.RIGHT
                ? Text.translatable("rune.menu.gear.active_right", "Active Slot: right-click in air")
                : Text.translatable("rune.menu.gear.active_left", "Active Slot: left-click in air");
        return List.of(note.color(NamedTextColor.GOLD));
    }
}
