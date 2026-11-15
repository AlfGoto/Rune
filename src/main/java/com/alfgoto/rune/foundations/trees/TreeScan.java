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

/**
 * The shared tree scan (see {@code docs/jobs/woodcutting.md}): from a block, follows natural logs of the same wood
 * type that touch each other (sides, edges and corners), in every direction.
 *
 * <p>"Natural" means not tagged {@link BlockTag#PLACED}. The origin block is never part of the result. Main thread
 * only.
 */
public final class TreeScan {

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
