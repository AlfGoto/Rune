package com.alfgoto.rune.gameplay.woodcutting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Tree Dash: finds the nearest log the player can see and sends the player towards it. */
public final class TreeDash {

    /** Effect id of Tree Dash: the range in blocks. */
    public static final String TREE_DASH = "tree_dash";

    /** At most this many of the nearest logs are ray traced per click, to keep the main thread cheap. */
    private static final int MAX_RAY_TRACES = 10;

    private TreeDash() {}

    /** The nearest log within {@code range} blocks of the player's eyes that nothing hides from them. */
    public static Optional<Block> nearestVisibleLog(Player player, double range) {
        Location eye = player.getEyeLocation();
        int max = (int) Math.floor(range);
        Block center = eye.getBlock();
        List<Block> logs = new ArrayList<>();
        double squared = range * range;
        for (int x = -max; x <= max; x++) {
            for (int y = -max; y <= max; y++) {
                for (int z = -max; z <= max; z++) {
                    if (x * x + y * y + z * z <= squared) {
                        Block block = center.getRelative(x, y, z);
                        if (Tag.LOGS.isTagged(block.getType())) {
                            logs.add(block);
                        }
                    }
                }
            }
        }
        Vector from = eye.toVector();
        logs.sort(Comparator.comparingDouble(log -> centerOf(log).distanceSquared(from)));
        int traces = 0;
        for (Block log : logs) {
            if (traces++ >= MAX_RAY_TRACES) {
                break;
            }
            Vector to = centerOf(log);
            double distance = to.distance(from);
            if (distance > range) {
                continue;
            }
            Vector direction = to.clone().subtract(from).normalize();
            RayTraceResult hit =
                    eye.getWorld().rayTraceBlocks(eye, direction, distance + 1, FluidCollisionMode.NEVER, true);
            if (hit != null && log.equals(hit.getHitBlock())) {
                return Optional.of(log);
            }
        }
        return Optional.empty();
    }

    /**
     * Pushes the player towards {@code log}. The push grows with the distance ({@code speedPerBlock}) up to
     * {@code maxSpeed}, so a close log is a short hop.
     */
    public static void dash(Player player, Block log, double speedPerBlock, double maxSpeed) {
        Vector to = centerOf(log).subtract(player.getEyeLocation().toVector());
        double distance = to.length();
        if (distance < 1.0E-6) {
            return;
        }
        double speed = Math.min(maxSpeed, distance * speedPerBlock);
        player.setVelocity(to.normalize().multiply(speed));
    }

    private static Vector centerOf(Block block) {
        return block.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));
    }
}
