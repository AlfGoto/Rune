package com.alfgoto.rune.foundations.trees;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * The shared tree scan (see {@code docs/jobs/woodcutting.md}): from a block, follows natural logs of the same wood
 * type that touch each other (sides, edges and corners), in every direction.
 *
 * <p>"Natural" means not tagged {@link BlockTag#PLACED}. The origin block is never part of the result. Main thread
 * only.
 */
public final class TreeScan {

    /** How many canopy blocks in a row can lie between a log and a felled canopy block. */
    public static final int CANOPY_REACH = 4;

    private static final BlockFace[] FACES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final BlockTracker tracker;

    public TreeScan(BlockTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Scans from {@code origin} and returns the connected natural logs of its wood type, without the origin.
     *
     * @param cap max logs returned
     */
    public List<Block> scan(Block origin, int cap) {
        List<Block> logs = new ArrayList<>();
        String wood = woodType(origin.getType());
        if (wood == null || cap <= 0) {
            return logs;
        }
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
                        if (logs.size() < cap && !seen.contains(next) && isNaturalLog(next, wood)) {
                            seen.add(next);
                            logs.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
        }
        return logs;
    }

    /**
     * The Treefeller scan (docs/jobs/woodcutting.md): the natural logs of the origin's wood type that connect to it
     * and are at or above its height, then the canopy blocks (leaves, wart blocks, shroomlight) that touch those
     * logs or the origin by a face. Everything counts towards {@code cap}: logs come first (nearest first), the
     * canopy fills what is left. The origin is not in the result. Canopy tagged {@link BlockTag#PLACED} is left
     * alone.
     *
     * @param cap max blocks returned (logs and canopy together)
     */
    public List<Block> scanTree(Block origin, int cap) {
        List<Block> result = new ArrayList<>();
        String wood = woodType(origin.getType());
        if (wood == null || cap <= 0) {
            return result;
        }
        List<Block> trunk = new ArrayList<>();
        trunk.add(origin);
        Set<Block> seen = new HashSet<>();
        seen.add(origin);
        for (int i = 0; i < trunk.size() && result.size() < cap; i++) {
            Block current = trunk.get(i);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block next = current.getRelative(dx, dy, dz);
                        if (result.size() < cap
                                && next.getY() >= origin.getY()
                                && !seen.contains(next)
                                && isNaturalLog(next, wood)) {
                            seen.add(next);
                            trunk.add(next);
                            result.add(next);
                        }
                    }
                }
            }
        }
        // Canopy: starts on the faces of the logs, then spreads face to face through canopy blocks, at most
        // CANOPY_REACH blocks away from a log (a huge fungus hat or a big oak crown is wider than its trunk).
        Deque<Block> queue = new ArrayDeque<>(trunk);
        Map<Block, Integer> depth = new HashMap<>();
        for (Block log : trunk) {
            depth.put(log, 0);
        }
        while (!queue.isEmpty() && result.size() < cap) {
            Block current = queue.poll();
            int nextDepth = depth.get(current) + 1;
            if (nextDepth > CANOPY_REACH) {
                continue;
            }
            for (BlockFace face : FACES) {
                Block next = current.getRelative(face);
                if (result.size() >= cap) {
                    break;
                }
                boolean firstVisit = seen.add(next);
                if (firstVisit && isNaturalCanopy(next)) {
                    result.add(next);
                    depth.put(next, nextDepth);
                    queue.add(next);
                }
            }
        }
        return result;
    }

    /**
     * The Great Fell scan (docs/jobs/woodcutting.md): every tree that has a natural log within {@code radius}
     * blocks of {@code center}. Each tree is whole: its trunk from the lowest connected log up, plus its canopy
     * (see {@link #scanTree}), even where it reaches past the radius. Trees closest to the center come first.
     *
     * @param treeCap max blocks per tree
     * @param maxBlocks max blocks returned in total
     */
    public List<Block> scanForest(Block center, int radius, int treeCap, int maxBlocks) {
        List<Block> logs = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz <= radius * radius
                            && isNaturalLog(center.getRelative(dx, dy, dz))) {
                        logs.add(center.getRelative(dx, dy, dz));
                    }
                }
            }
        }
        logs.sort(java.util.Comparator.comparingDouble(
                log -> log.getLocation().distanceSquared(center.getLocation())));
        Set<Block> taken = new HashSet<>();
        List<Block> result = new ArrayList<>();
        for (Block log : logs) {
            if (result.size() >= maxBlocks) {
                break;
            }
            if (taken.contains(log)) {
                continue;
            }
            List<Block> connected = new ArrayList<>(scan(log, treeCap));
            connected.add(log);
            Block lowest = log;
            for (Block other : connected) {
                if (other.getY() < lowest.getY()) {
                    lowest = other;
                }
            }
            List<Block> tree = new ArrayList<>();
            tree.add(lowest);
            tree.addAll(scanTree(lowest, treeCap - 1));
            for (Block block : tree) {
                if (taken.add(block) && result.size() < maxBlocks) {
                    result.add(block);
                }
            }
            taken.addAll(connected);
        }
        return result;
    }

    private boolean isNaturalCanopy(Block block) {
        Material type = block.getType();
        boolean canopy = Tag.LEAVES.isTagged(type)
                || type == Material.NETHER_WART_BLOCK
                || type == Material.WARPED_WART_BLOCK
                || type == Material.SHROOMLIGHT;
        return canopy && !tracker.has(block, BlockTag.PLACED);
    }

    /** True when the block is a log that is not tagged {@link BlockTag#PLACED}. */
    boolean isNaturalLog(Block block) {
        return isNaturalLog(block, woodType(block.getType()));
    }

    /** True when the block is a natural log of the given wood type. */
    boolean isNaturalLog(Block block, String wood) {
        return wood != null
                && wood.equals(woodType(block.getType()))
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
