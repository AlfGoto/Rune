package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.text.Rarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** One result of a roll. Rolling gives nothing: the caller decides when to {@link #give}. */
public interface Drop {

    /** What the player sees, e.g. "Test Rune" or "+100 Mining XP". */
    Component describe();

    /** The Rarity of this result, or {@code null} when it has none (XP, Reroll Token). */
    default Rarity rarity() {
        return null;
    }

    /** An item stack that shows this result in a menu. Never {@code null}. */
    default ItemStack icon() {
        return new ItemStack(Material.PAPER);
    }

    /** Gives this result to {@code player}. Run on the main thread. */
    void give(Player player);
}
