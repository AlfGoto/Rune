package com.alfgoto.rune.gameplay.blast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** The blocks a blast breaks around the mined block: a 3x3 square (Blast Mine) or a 3x3x3 cube (Great Blast). */
public final class BlastArea {

    /** The axis of the face being mined: the 3x3 square of Blast Mine is flat against it. */
    public enum Axis {
        X,
        Y,
        Z
    }

    /** How far (blocks) the player's look is traced to find the face they mine. */
    private static final double FACE_REACH = 8;

    private BlastArea() {}

    /** The 8 blocks around {@code origin} in the plane flat to {@code axis}, nearest first, that {@code accept}. */
    public static List<Block> square(Block origin, Axis axis, Predicate<Block> accept) {
        return around(origin, axis, accept);
    }

    /** The 26 blocks around {@code origin}, nearest first, that {@code accept}. */
    public static List<Block> cube(Block origin, Predicate<Block> accept) {
        return around(origin, null, accept);
    }

    private static List<Block> around(Block origin, Axis flatAxis, Predicate<Block> accept) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    boolean outOfPlane = (flatAxis == Axis.X && x != 0)
                            || (flatAxis == Axis.Y && y != 0)
                            || (flatAxis == Axis.Z && z != 0);
                    if ((x == 0 && y == 0 && z == 0) || outOfPlane) {
                        continue;
                    }
                    Block block = origin.getRelative(x, y, z);
                    if (accept.test(block)) {
                        blocks.add(block);
                    }
                }
            }
        }
        blocks.sort(Comparator.comparingInt((Block b) -> distanceSquared(origin, b)));
        return blocks;
    }

    private static int distanceSquared(Block a, Block b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * The axis of the face the player is mining on {@code block}: the face their look hits, or when that is not
     * this block, the axis they look along the most.
     */
    public static Axis faceAxis(Player player, Block block) {
        Location eye = player.getEyeLocation();
        RayTraceResult hit =
                player.getWorld().rayTraceBlocks(eye, eye.getDirection(), FACE_REACH, FluidCollisionMode.NEVER, true);
        if (hit != null && block.equals(hit.getHitBlock()) && hit.getHitBlockFace() != null) {
            return axisOf(new Vector(
                    hit.getHitBlockFace().getModX(),
                    hit.getHitBlockFace().getModY(),
                    hit.getHitBlockFace().getModZ()));
        }
        return axisOf(eye.getDirection());
    }

    /** The axis a direction points along the most. */
    public static Axis axisOf(Vector direction) {
        double x = Math.abs(direction.getX());
        double y = Math.abs(direction.getY());
        double z = Math.abs(direction.getZ());
        if (y >= x && y >= z) {
            return Axis.Y;
        }
        return x >= z ? Axis.X : Axis.Z;
    }
}
