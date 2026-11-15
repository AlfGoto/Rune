package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.text.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * A chest menu. Items in it can never be taken out ({@link MenuListener} cancels every click).
 * A menu opened from another menu knows its parent and shows a back button in the middle of the
 * bottom row.
 */
public class Menu implements InventoryHolder {

    private final Inventory inventory;
    private final Menu parent;
    private final Map<Integer, Button> buttons = new HashMap<>();

    /** A menu with no parent (no back button). */
    public Menu(Component title, int rows) {
        this(title, rows, null);
    }

    /** A menu opened from {@code parent}; its back button returns there. */
    public Menu(Component title, int rows, Menu parent) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be 1 to 6, got " + rows);
        }
        this.parent = parent;
        this.inventory = Bukkit.createInventory(this, rows * 9, title);
        if (parent != null) {
            set(backSlot(), backButton());
        }
    }

    public Menu parent() {
        return parent;
    }

    public int size() {
        return inventory.getSize();
    }

    /** Puts {@code button} in {@code slot} and redraws it. */
    public void set(int slot, Button button) {
        Objects.checkIndex(slot, size());
        buttons.put(slot, Objects.requireNonNull(button, "button"));
        inventory.setItem(slot, button.icon());
    }

    /** Removes everything and redraws the back button. */
    protected void clear() {
        buttons.clear();
        inventory.clear();
        if (parent != null) {
            set(backSlot(), backButton());
        }
    }

    /** The slot of the back button: bottom row, middle. */
    protected final int backSlot() {
        return size() - 5;
    }

    private Button backButton() {
        return Button.of(
                Material.ARROW, Text.translatable("rune.menu.back", "Back").color(NamedTextColor.YELLOW), this::back);
    }

    /** Opens the parent menu, or closes this menu if it has none. */
    public void back(Player player) {
        if (parent == null) {
            player.closeInventory();
        } else {
            parent.open(player);
        }
    }

    /** Shows this menu to {@code player}. */
    public void open(Player player) {
        player.openInventory(inventory);
    }

    /** Runs the button in {@code slot}, if any. Called by {@link MenuListener}. */
    void click(Player player, int slot) {
        Button button = buttons.get(slot);
        if (button != null) {
            button.onClick().accept(player);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
