package com.alfgoto.rune.visuals.glow;

import com.alfgoto.rune.gameplay.runes.HeldTraits;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Draws the glow wanted by {@link HeldTraits}: an invisible Light block at the player's head, sent to nearby
 * players only (never placed in the world), so nothing is left behind. Reads gameplay data, changes none.
 */
public final class GlowLight implements Runnable {

    /** The Light block is sent again every this many checks, in case a chunk update erased it. */
    private static final int RESEND_EVERY = 10;

    private static final double VIEW_DISTANCE = 64;

    private static final class Glow {
        final Location location;
        final int level;
        int age;

        Glow(Location location, int level) {
            this.location = location;
            this.level = level;
        }
    }

    private final Plugin plugin;
    private final HeldTraits held;
    private final Map<UUID, Glow> glows = new HashMap<>();

    public GlowLight(Plugin plugin, HeldTraits held) {
        this.plugin = plugin;
        this.held = held;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            glow(player, held.glowLevel(player.getUniqueId()));
        }
    }

    /** Removes the Light block of one player from every view (on quit). */
    public void clear(UUID id) {
        Glow glow = glows.remove(id);
        if (glow != null) {
            restore(glow);
        }
    }

    public void clearAll() {
        for (Glow glow : glows.values()) {
            restore(glow);
        }
        glows.clear();
    }

    private void glow(Player player, int level) {
        UUID id = player.getUniqueId();
        Glow old = glows.get(id);
        Block head = level > 0 ? player.getEyeLocation().getBlock() : null;
        if (head != null && (head.isSolid() || head.isLiquid())) {
            head = null; // the head is inside a solid block or liquid (plants do not count): no room for light
        }
        if (head == null) {
            if (old != null) {
                restore(old);
                glows.remove(id);
            }
            return;
        }
        Location at = head.getLocation();
        if (old != null) {
            if (old.location.equals(at) && old.level == level) {
                if (++old.age % RESEND_EVERY == 0) {
                    show(old);
                }
                return;
            }
            restore(old);
        }
        Glow glow = new Glow(at, level);
        glows.put(id, glow);
        show(glow);
    }

    private void show(Glow glow) {
        BlockData light = Bukkit.createBlockData("minecraft:light[level=" + glow.level + "]");
        for (Player viewer : glow.location.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(glow.location) <= VIEW_DISTANCE * VIEW_DISTANCE) {
                viewer.sendBlockChange(glow.location, light);
            }
        }
    }

    private void restore(Glow glow) {
        BlockData real = glow.location.getBlock().getBlockData();
        for (Player viewer : glow.location.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(glow.location) <= VIEW_DISTANCE * VIEW_DISTANCE) {
                viewer.sendBlockChange(glow.location, real);
            }
        }
    }
}
