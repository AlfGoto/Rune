package com.alfgoto.rune.gameplay.items;

import org.bukkit.inventory.ItemStack;

/**
 * Draws the look of an item (name, lore) from its data. Visuals implement it and register it on
 * the {@link ItemRegistry}; gameplay never depends on them (ADR 0003).
 */
@FunctionalInterface
public interface ItemRedrawer {

    /** Rewrites the name and lore of {@code stack} from scratch. Must be safe to call again. */
    void redraw(ItemStack stack, RuneItem item);
}
