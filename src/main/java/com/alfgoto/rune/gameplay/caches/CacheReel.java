package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * Shows the opening of a Cache (the reel). Gameplay only knows this interface: Visuals provides the
 * menu (see {@code Caches#setReel}). The loot is already rolled; {@code finish} gives it.
 */
@FunctionalInterface
public interface CacheReel {

    /**
     * Plays the reel for {@code player}, then runs {@code finish} (it gives the loot). It must run
     * {@code finish} even when the player closes the menu early. Running it twice is harmless.
     */
    void play(Player player, Rarity rarity, List<Drop> drops, Runnable finish);
}
