package com.alfgoto.rune.gameplay.woodcutting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

/**
 * Chorus Axe: when the player is hurt (and survives) with the axe in the main hand, they teleport a short
 * way to a safe spot, like a chorus fruit. The range is the Trait value; a cooldown (config) stops chains.
 */
final class HurtTeleportListener implements Listener {

    /** Spots tried before giving up (then the player simply stays). */
    private static final int ATTEMPTS = 24;

    private final Plugin plugin;
    private final HeldAxe held;
    private final long cooldownMillis;
    private final DoubleSupplier random;
    private final LongSupplier clock;
    private final Map<UUID, Long> lastTeleport = new HashMap<>();

    HurtTeleportListener(
            Plugin plugin, HeldAxe held, long cooldownMillis, DoubleSupplier random, LongSupplier clock) {
        this.plugin = plugin;
        this.held = held;
        this.cooldownMillis = cooldownMillis;
        this.random = random;
        this.clock = clock;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHurt(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getFinalDamage() <= 0) {
            return;
        }
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            return;
        }
        double range = held.value(player, AxeTraits.HURT_TELEPORT);
        if (range <= 0) {
            return;
        }
        long now = clock.getAsLong();
        Long last = lastTeleport.get(player.getUniqueId());
        if (last != null && AxeTraits.onCooldown(last, now, cooldownMillis)) {
            return;
        }
        Location target = findSpot(player.getLocation(), range);
        if (target == null) {
            return;
        }
        lastTeleport.put(player.getUniqueId(), now);
        Location from = player.getLocation();
        // One tick later: the damage event is still being handled.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline() && !player.isDead() && player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                World world = from.getWorld();
                world.spawnParticle(Particle.PORTAL, from.clone().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.2);
                world.playSound(from, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1f);
                player.playSound(target, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1f);
            }
        });
    }

    /** A spot within {@code range} blocks (flat distance) with solid ground and two free blocks above. */
    private Location findSpot(Location around, double range) {
        World world = around.getWorld();
        for (int i = 0; i < ATTEMPTS; i++) {
            // The reach shrinks with each try, so a small island (like the End platform) still gets a spot.
            double reach = Math.max(1, range * (ATTEMPTS - i) / ATTEMPTS);
            double[] offset = AxeTraits.teleportOffset(reach, random.getAsDouble(), random.getAsDouble());
            int x = around.getBlockX() + (int) Math.round(offset[0]);
            int z = around.getBlockZ() + (int) Math.round(offset[1]);
            int top = Math.min(world.getMaxHeight() - 2, around.getBlockY() + 3);
            int bottom = Math.max(world.getMinHeight() + 1, around.getBlockY() - (int) range);
            for (int y = top; y >= bottom; y--) {
                Block feet = world.getBlockAt(x, y, z);
                Block ground = feet.getRelative(0, -1, 0);
                if (ground.getType().isSolid() && free(feet) && free(feet.getRelative(0, 1, 0))) {
                    return new Location(world, x + 0.5, y, z + 0.5, around.getYaw(), around.getPitch());
                }
            }
        }
        return null;
    }

    private static boolean free(Block block) {
        return block.isPassable() && !block.isLiquid() && block.getType() != Material.FIRE
                && block.getType() != Material.SOUL_FIRE && block.getType() != Material.MAGMA_BLOCK
                && block.getType() != Material.LAVA;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastTeleport.remove(event.getPlayer().getUniqueId());
    }
}
