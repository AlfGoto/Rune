package com.alfgoto.rune.gameplay.woodcutting;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Mangrove Axe: while it is in the main hand, water does not slow the player down. The vanilla water
 * movement efficiency (0 to 1) is pushed to 1 by a temporary modifier. Vanilla mud does not slow players,
 * so nothing is needed there. The underwater mining penalty is cancelled by the shared {@code no_water_penalty}
 * effect (HeldTraits).
 */
final class WaterStrideListener {

    private final Plugin plugin;
    private final HeldAxe held;
    private final NamespacedKey key;

    WaterStrideListener(Plugin plugin, HeldAxe held) {
        this.plugin = plugin;
        this.held = held;
        this.key = new NamespacedKey(plugin, "water_stride");
    }

    /** Starts the repeating check (every 5 ticks). */
    void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 5L, 5L);
    }

    private void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            AttributeInstance efficiency = player.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY);
            if (efficiency == null) {
                continue;
            }
            boolean on = held.value(player, AxeTraits.WATER_STRIDE) > 0;
            AttributeModifier current = efficiency.getModifier(key);
            if (on && current == null) {
                efficiency.addTransientModifier(
                        new AttributeModifier(key, 1, AttributeModifier.Operation.ADD_NUMBER));
            } else if (!on && current != null) {
                efficiency.removeModifier(key);
            }
        }
    }
}
