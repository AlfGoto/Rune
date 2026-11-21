package com.alfgoto.rune.gameplay.runes;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.plugin.Plugin;

/**
 * Applies the speed bonus when the player starts mining a block. The bonus is a temporary modifier on the
 * player's block break speed. It is replaced at the start of every block, so it always matches the tool in
 * hand and the block being mined.
 */
final class SpeedListener implements Listener {

    /** The lowest speed change: -95%. */
    private static final double MIN_FACTOR = -0.95;

    private final SpeedBonuses bonuses;
    private final NamespacedKey key;

    SpeedListener(Plugin plugin, SpeedBonuses bonuses) {
        this.bonuses = bonuses;
        this.key = new NamespacedKey(plugin, "mining_speed");
    }

    @EventHandler
    public void onStartMining(BlockDamageEvent event) {
        Player player = event.getPlayer();
        AttributeInstance speed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(key);
        double percent = bonuses.percent(event.getItemInHand(), player, event.getBlock());
        if (percent != 0) {
            // A Trait drawback can slow mining down, but never to a full stop.
            double factor = Math.max(percent / 100, MIN_FACTOR);
            speed.addTransientModifier(
                    new AttributeModifier(key, factor, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }
}
