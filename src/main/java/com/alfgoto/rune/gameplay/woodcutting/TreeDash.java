package com.alfgoto.rune.gameplay.woodcutting;

import java.util.Optional;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Tree Dash: sends the player towards the log they are looking at. */
public final class TreeDash {

    /** Effect id of Tree Dash: the range in blocks. */
    public static final String TREE_DASH = "tree_dash";

    private TreeDash() {}

    /** What the player looks at: a log and its distance from the eyes. */
    public record Target(Block log, double distance) {}

    /**
     * The log the player is looking at, within {@code reach} blocks of the eyes. Empty when the player looks at
     * something else (or nothing). The caller compares the distance with the Rune range.
     */
    public static Optional<Target> lookedAtLog(Player player, double reach) {
        Location eye = player.getEyeLocation();
        RayTraceResult hit =
                player.getWorld().rayTraceBlocks(eye, eye.getDirection(), reach, FluidCollisionMode.NEVER, true);
        if (hit == null || hit.getHitBlock() == null || !Tag.LOGS.isTagged(hit.getHitBlock().getType())) {
            return Optional.empty();
        }
        return Optional.of(new Target(hit.getHitBlock(), hit.getHitPosition().distance(eye.toVector())));
    }

    /**
     * Pushes the player towards {@code log}. The push grows with the distance ({@code speedPerBlock}) up to
     * {@code maxSpeed}, so a close log is a short hop. The push always has at least {@code lift} upwards, so the
     * player leaves the ground: ground friction would stop a flat push after about 3 blocks.
     */
    public static void dash(Player player, Block log, double speedPerBlock, double maxSpeed, double lift) {
        Vector to = centerOf(log).subtract(player.getEyeLocation().toVector());
        double distance = to.length();
        if (distance < 1.0E-6) {
            return;
        }
        double speed = Math.min(maxSpeed, distance * speedPerBlock);
        Vector push = to.normalize().multiply(speed);
        push.setY(Math.max(push.getY(), lift));
        player.setVelocity(push);
    }

    private static Vector centerOf(Block block) {
        return block.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));
    }
}
