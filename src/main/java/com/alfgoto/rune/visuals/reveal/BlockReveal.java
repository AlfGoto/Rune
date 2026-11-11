package com.alfgoto.rune.visuals.reveal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Outlines blocks through walls for one player, for a limited time.
 *
 * <p>Each block gets a glowing block display that only that player can see. The displays are not
 * saved with the world and are removed when the time is up, when the player leaves, and when the
 * plugin is disabled. Ore Sense, Bone, Echo, Warden Pickaxe and Worldheart use this.
 */
public final class BlockReveal {

    private static final float SCALE = 1.002f;
    private static final float OFFSET = -(SCALE - 1f) / 2f;

    private final Plugin plugin;
    private final Map<UUID, List<Reveal>> active = new HashMap<>();

    public BlockReveal(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Outlines the given blocks for one player.
     *
     * @param ticks how long the outlines stay, in ticks (at least 1)
     * @param color outline colour
     */
    public void reveal(Player player, Collection<Block> blocks, int ticks, Color color) {
        if (blocks.isEmpty()) {
            return;
        }
        List<BlockDisplay> displays = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            displays.add(spawn(player, block, color));
        }
        Reveal reveal = new Reveal(displays);
        UUID playerId = player.getUniqueId();
        active.computeIfAbsent(playerId, id -> new ArrayList<>()).add(reveal);
        reveal.task = plugin.getServer()
                .getScheduler()
                .runTaskLater(plugin, () -> end(playerId, reveal), Math.max(1, ticks));
    }

    /** Removes every outline of one player now. */
    public void clear(UUID playerId) {
        List<Reveal> reveals = active.remove(playerId);
        if (reveals != null) {
            reveals.forEach(Reveal::remove);
        }
    }

    /** Removes every outline of every player now. */
    public void clearAll() {
        active.values().forEach(reveals -> reveals.forEach(Reveal::remove));
        active.clear();
    }

    /** Number of outlines currently shown to a player. */
    public int count(UUID playerId) {
        return active.getOrDefault(playerId, List.of()).stream()
                .mapToInt(reveal -> reveal.displays.size())
                .sum();
    }

    private void end(UUID playerId, Reveal reveal) {
        List<Reveal> reveals = active.get(playerId);
        if (reveals != null) {
            reveals.remove(reveal);
            if (reveals.isEmpty()) {
                active.remove(playerId);
            }
        }
        reveal.remove();
    }

    private BlockDisplay spawn(Player player, Block block, Color color) {
        Location location = block.getLocation();
        return block.getWorld().spawn(location, BlockDisplay.class, display -> {
            display.setVisibleByDefault(false);
            display.setPersistent(false);
            display.setBlock(block.getBlockData());
            display.setGlowing(true);
            display.setGlowColorOverride(color);
            display.setTransformation(new Transformation(
                    new Vector3f(OFFSET, OFFSET, OFFSET),
                    new Quaternionf(),
                    new Vector3f(SCALE, SCALE, SCALE),
                    new Quaternionf()));
            player.showEntity(plugin, display);
        });
    }

    private static final class Reveal {
        private final List<BlockDisplay> displays;
        private BukkitTask task;

        private Reveal(List<BlockDisplay> displays) {
            this.displays = displays;
        }

        private void remove() {
            if (task != null) {
                task.cancel();
            }
            displays.forEach(BlockDisplay::remove);
        }
    }
}
