package com.alfgoto.rune.visuals.menu;

import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
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
}
