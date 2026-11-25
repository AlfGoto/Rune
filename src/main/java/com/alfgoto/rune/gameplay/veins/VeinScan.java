package com.alfgoto.rune.gameplay.veins;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.block.Block;

/** Finds the blocks of a vein: blocks that touch each other (sides, edges and corners) and are accepted. */
public final class VeinScan {

    private VeinScan() {}

    /**
     * The blocks connected to {@code origin} that {@code accept} says yes to, nearest first, at most {@code limit}
     * of them. {@code origin} itself is not in the result.
     */
    public static List<Block> find(Block origin, int limit, Predicate<Block> accept) {
        List<Block> found = new ArrayList<>();
        if (limit <= 0) {
            return found;
        }
        Set<Block> seen = new HashSet<>();
        seen.add(origin);
        Queue<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        while (!queue.isEmpty() && found.size() < limit) {
            Block current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (found.size() >= limit) {
                            return found;
                        }
                        Block next = current.getRelative(dx, dy, dz);
                        if ((dx != 0 || dy != 0 || dz != 0) && seen.add(next) && accept.test(next)) {
                            found.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
        }
        return found;
    }
}
