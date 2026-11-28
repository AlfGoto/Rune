package com.alfgoto.rune.visuals.unlocktree;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

/**
 * The few things the Unlock Tree needs from the advancement system. One place to change when the loading
 * moves from Paper's runtime API to a generated datapack (docs/design-decisions.md, Unlock Tree).
 */
interface AdvancementBackend {

    /** Adds the advancement to the server, replacing an older one with the same id. In memory only. */
    void load(NamespacedKey key, String json);

    /** True when the player has this node. */
    boolean isDone(Player player, NamespacedKey key);

    /** Gives the node to the player (the vanilla pop-up shows if the node asks for it). */
    void award(Player player, NamespacedKey key);

    /** Takes the node back from the player. */
    void revoke(Player player, NamespacedKey key);
}
