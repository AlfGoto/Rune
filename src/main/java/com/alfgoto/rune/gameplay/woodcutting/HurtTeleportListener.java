package com.alfgoto.rune.gameplay.woodcutting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    /** Players who asked {@code /rune debug chorus}: they see why each hit did or did not teleport. */
    private final Set<UUID> debugging = new HashSet<>();

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
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double range = held.value(player, AxeTraits.HURT_TELEPORT);
        debug(player, "hit", "Chorus: hit, damage %s, range %s",
                String.format("%.1f", event.getFinalDamage()), String.format("%.1f", range));
        if (event.getFinalDamage() <= 0) {
            debug(player, "no_damage", "Chorus: no teleport, the hit did no damage");
            return;
        }
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            debug(player, "fatal", "Chorus: no teleport, the hit kills you");
            return;
        }
        if (range <= 0) {
            debug(player, "no_trait", "Chorus: no teleport, no Chorus Axe in the main hand");
            return;
        }
        long now = clock.getAsLong();
        Long last = lastTeleport.get(player.getUniqueId());
        if (last != null && AxeTraits.onCooldown(last, now, cooldownMillis)) {
            debug(player, "cooldown", "Chorus: no teleport, cooldown");
            return;
        }
        Location target = findSpot(player.getLocation(), range);
        if (target == null) {
            debug(player, "no_spot", "Chorus: no teleport, no safe spot found");
            return;
        }
        lastTeleport.put(player.getUniqueId(), now);
        Location from = player.getLocation();
        // One tick later: the damage event is still being handled.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || player.isDead()) {
                return;
            }
            if (!player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                debug(player, "refused", "Chorus: no teleport, the server refused it");
                return;
            }
            debug(player, "done", "Chorus: teleported to %s %s %s",
                    target.getBlockX(), target.getBlockY(), target.getBlockZ());
            World world = from.getWorld();
            world.spawnParticle(Particle.PORTAL, from.clone().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.2);
            world.playSound(from, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1f);
            player.playSound(target, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1f);
        });
    }

    /** Turns the debug messages on or off for {@code player}. Returns true when they are now on. */
    boolean toggleDebug(Player player) {
        if (debugging.remove(player.getUniqueId())) {
            return false;
        }
        debugging.add(player.getUniqueId());
        return true;
    }

    private void debug(Player player, String key, String fallback, Object... args) {
        if (!debugging.contains(player.getUniqueId())) {
            return;
        }
        Component[] parts = new Component[args.length];
        for (int i = 0; i < args.length; i++) {
            parts[i] = Component.text(String.valueOf(args[i]));
        }
        player.sendMessage(Component.translatable("rune.debug.chorus." + key, fallback, parts)
                .color(NamedTextColor.GRAY));
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
                if (ground.getType().isSolid() && ground.getType() != Material.MAGMA_BLOCK && free(feet) && free(feet.getRelative(0, 1, 0))) {
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
        debugging.remove(event.getPlayer().getUniqueId());
    }
}
