package com.alfgoto.rune.gameplay.rewards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** One result of a roll. Rolling gives nothing: the caller decides when to {@link #give}. */
public interface Drop {

    /** What the player sees, e.g. "Test Rune" or "+100 Mining XP". */
    Component describe();

    /** Gives this result to {@code player}. Run on the main thread. */
    void give(Player player);

    /**
     * An item showing this result (the Cache reel shows it); never given. Default: a paper named
     * {@link #describe()}; results that are items show the item itself.
     */
    default ItemStack icon() {
        return named(Material.PAPER, describe());
    }

    /** One {@code material} named {@code name} (not in italics). */
    static ItemStack named(Material material, Component name) {
        ItemStack icon = new ItemStack(material);
        icon.editMeta(meta -> meta.displayName(name.decoration(TextDecoration.ITALIC, false)));
        return icon;
    }
}
