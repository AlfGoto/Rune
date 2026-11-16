package com.alfgoto.rune.gameplay.runes;

import org.bukkit.entity.Player;

/** Hears what happens to Active Runes, to show it to the player. Registered with {@link ActiveRunes#addListener}. */
public interface ActiveRuneListener {

    /** The Rune ran and is now on cooldown for {@code seconds}. */
    void onUsed(Player player, Rune rune, double seconds);

    /** The click was refused: the Rune is still on cooldown for {@code secondsLeft}. */
    void onCooldown(Player player, Rune rune, double secondsLeft);
}
