package com.alfgoto.rune.gameplay.rewards;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/** One result of a roll. Rolling gives nothing: the caller decides when to {@link #give}. */
public interface Drop {

    /** What the player sees, e.g. "Test Rune" or "+100 Mining XP". */
    Component describe();

    /** Gives this result to {@code player}. Run on the main thread. */
    void give(Player player);
}
