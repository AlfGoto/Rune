package com.alfgoto.rune.foundations.trees;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * The shared tree scan (Treefeller rules, see {@code docs/jobs/woodcutting.md}): from a broken block, follows
 * natural logs of the same wood type that touch each other (sides, edges and corners), plus the canopy blocks
 * (leaves, wart blocks, shroomlight) touching those logs.
 *
 * <p>"Natural" means not tagged {@link BlockTag#PLACED}. The origin block is never part of the result: it is
 * the block being broken. Everything counts towards the cap. Main thread only.
 */
public final class TreeScan {

    /** What a scan found. Logs come first in the cap, then canopy. */
    public record Result(List<Block> logs, List<Block> canopy) {}

    private static final Set<Material> CANOPY_EXTRA =
            Set.of(Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK, Material.SHROOMLIGHT);

    private static final BlockFace[] SIDES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final BlockTracker tracker;

    public TreeScan(BlockTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Scans from {@code origin}.
     *
     * @param cap max blocks returned (logs and canopy together)
     * @param aboveOnly only logs at or above the origin's height (Treefeller). When false, the whole connected
     *     trunk is followed: Felled Tree needs that to know whether logs are left below.
     */
    public Result scan(Block origin, int cap, boolean aboveOnly) {
        List<Block> logs = new ArrayList<>();
        List<Block> canopy = new ArrayList<>();
        String wood = woodType(origin.getType());
        if (wood == null || cap <= 0) {
            return new Result(logs, canopy);
        }
        int minY = aboveOnly ? origin.getY() : Integer.MIN_VALUE;
        Set<Block> seen = new HashSet<>();
        seen.add(origin);
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        while (!queue.isEmpty() && logs.size() < cap) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block next = current.getRelative(dx, dy, dz);
                        if (logs.size() < cap
                                && next.getY() >= minY
                                && !seen.contains(next)
                                && isNaturalLog(next, wood)) {
                            seen.add(next);
                            logs.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
        }
        Set<Block> canopySeen = new HashSet<>();
        for (Block log : logs) {
            for (BlockFace face : SIDES) {
                if (logs.size() + canopy.size() >= cap) {
                    return new Result(logs, canopy);
                }
                Block next = log.getRelative(face);
                if (isNaturalCanopy(next) && canopySeen.add(next)) {
                    canopy.add(next);
                }
            }
        }
        return new Result(logs, canopy);
    }

    /** True when {@code origin} is a natural log and no other natural log of its wood type is connected to it. */
    public boolean isLastLog(Block origin) {
        String wood = woodType(origin.getType());
        return isNaturalLog(origin, wood) && scan(origin, 1, false).logs().isEmpty();
    }

    private boolean isNaturalLog(Block block, String wood) {
        return wood != null
                && wood.equals(woodType(block.getType()))
                && !tracker.has(block, BlockTag.PLACED);
    }

    private boolean isNaturalCanopy(Block block) {
        Material type = block.getType();
        return (Tag.LEAVES.isTagged(type) || CANOPY_EXTRA.contains(type))
                && !tracker.has(block, BlockTag.PLACED);
    }

    /**
     * The wood type of a log-like block: "oak" for an oak log, oak wood, stripped oak log and stripped oak wood;
     * "crimson" for crimson stems and hyphae. Null when the block is not a log.
     */
    static String woodType(Material material) {
        if (!Tag.LOGS.isTagged(material)) {
            return null;
        }
        String name = material.name().toLowerCase(Locale.ROOT);
        if (name.startsWith("stripped_")) {
            name = name.substring("stripped_".length());
        }
        for (String suffix : List.of("_log", "_wood", "_stem", "_hyphae")) {
            if (name.endsWith(suffix)) {
                return name.substring(0, name.length() - suffix.length());
            }
        }
        return name;
    }
}
