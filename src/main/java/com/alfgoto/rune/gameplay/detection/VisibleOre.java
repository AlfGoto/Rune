package com.alfgoto.rune.gameplay.detection;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/** Finds the nearest ore a player can see (Ore Dash). */
public final class VisibleOre {

    private VisibleOre() {}

    /**
     * The nearest ore within {@code range} blocks of the player's eyes that they can see: a line from the eyes to
     * the ore's centre reaches the ore before any other block. Looks at the {@code maxChecks} nearest ores only,
     * so a click never costs much.
     */
    public static Optional<Block> nearest(Player player, double range, int maxChecks) {
        Location eye = player.getEyeLocation();
        Vector from = eye.toVector();
        List<Block> ores = OreScan.find(eye.getBlock(), (int) Math.ceil(range)).stream()
                .filter(ore -> center(ore).distance(from) <= range)
                .sorted(Comparator.comparingDouble(ore -> center(ore).distanceSquared(from)))
                .limit(Math.max(1, maxChecks))
                .toList();
        for (Block ore : ores) {
            Vector to = center(ore).subtract(from);
            double distance = to.length();
            if (distance < 1.0E-6) {
                return Optional.of(ore);
            }
            RayTraceResult hit = player.getWorld()
                    .rayTraceBlocks(eye, to.normalize(), distance + 1, FluidCollisionMode.NEVER, true);
            if (hit != null && ore.equals(hit.getHitBlock())) {
                return Optional.of(ore);
            }
        }
        return Optional.empty();
    }

    private static Vector center(Block block) {
        return block.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));
    }
}
