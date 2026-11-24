package com.alfgoto.rune.visuals.menu;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** One clickable icon in a {@link Menu}. */
public record Button(ItemStack icon, Consumer<Player> onClick) {

    public Button {
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(onClick, "onClick");
    }

    /** A button with a plain icon of {@code material} named {@code name}. */
    public static Button of(Material material, Component name, Consumer<Player> onClick) {
        return of(material, 1, name, onClick);
    }

    /** Same, with a stack size (useful to show a number). */
    public static Button of(Material material, int amount, Component name, Consumer<Player> onClick) {
        ItemStack icon = new ItemStack(material, amount);
        icon.editMeta(meta -> meta.displayName(name.decoration(TextDecoration.ITALIC, false)));
        return new Button(icon, onClick);
    }

    /** Model of an item drawn as nothing: the menu's background shows through (docs/art-direction.md, "Menus"). */
    public static final NamespacedKey BLANK = new NamespacedKey("rune", "menu_blank");

    /** Model of a dark empty recess: an empty Rune socket or Job card (hides the grey vanilla slot). */
    public static final NamespacedKey EMPTY = new NamespacedKey("rune", "menu_empty");

    /**
     * An invisible button over a part drawn by the menu's background: no icon, but its tooltip
     * ({@code name} then {@code lore}) and its click.
     */
    public static Button invisible(Component name, List<Component> lore, Consumer<Player> onClick) {
        return withModel(BLANK, name, lore, onClick);
    }

    /** A button drawn with the item model {@code model}, named {@code name}. */
    public static Button withModel(
            NamespacedKey model, Component name, List<Component> lore, Consumer<Player> onClick) {
        ItemStack icon = new ItemStack(Material.PAPER);
        icon.editMeta(meta -> {
            meta.setItemModel(model);
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            if (!lore.isEmpty()) {
                meta.lore(lore.stream()
                        .map(line -> line.decoration(TextDecoration.ITALIC, false))
                        .toList());
            }
        });
        return new Button(icon, onClick);
    }
}
