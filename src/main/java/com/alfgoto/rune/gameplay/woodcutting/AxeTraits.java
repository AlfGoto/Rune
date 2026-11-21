package com.alfgoto.rune.gameplay.woodcutting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/** The effect ids of the axe Trait extras and their pure rules, with no Bukkit in them so they can be tested. */
public final class AxeTraits {

    /** Birch Axe: move speed boost after each Felled Tree. The binding value is the duration in seconds. */
    public static final String FELLED_TREE_SPEED = "felled_tree_speed";

    /** Spruce Axe: no powder snow slowdown or freezing while held. The binding value is 1 (on). */
    public static final String POWDER_SNOW_IMMUNITY = "powder_snow_immunity";

    /** Poplar Axe: the leaves of a Felled Tree fall at once. The binding value is 1 (on). */
    public static final String FELLED_TREE_LEAF_FALL = "felled_tree_leaf_fall";

    /** Jungle Axe: climbing speed on vines, ladders and scaffolding. The binding value is the percent. */
    public static final String CLIMB_SPEED = "climb_speed";

    /** Vanilla climbing speed, in blocks per tick (a player climbs 0.2 blocks per tick). */
    static final double VANILLA_CLIMB_SPEED = 0.2;

    /** Vanilla horizontal slowdown in powder snow: the player keeps 90% of their speed. */
    static final double POWDER_SNOW_FACTOR = 0.9;

    /** Most leaves a Felled Tree can make fall (a safety cap for huge canopies). */
    static final int MAX_FALLING_LEAVES = 600;

    /** Leaves are at most this far from a log in vanilla (distance 7 decays). */
    static final int LEAF_REACH = 7;

    private AxeTraits() {}

    /** Upward speed while climbing with {@code percent} extra climbing speed. */
    public static double climbSpeed(double percent) {
        return VANILLA_CLIMB_SPEED * (1 + Math.max(0, percent) / 100);
    }

    /** Move speed bonus (as a fraction) that cancels the powder snow slowdown. */
    public static double powderSnowCompensation() {
        return 1 / POWDER_SNOW_FACTOR - 1;
    }

    /**
     * The leaves that fall with a Felled Tree: every leaf linked to {@code start} through a chain of leaves,
     * at most {@code maxDepth} steps away, in the order they are found, at most {@code cap} of them.
     *
     * @param start the last log of the tree (not part of the result)
     * @param neighbors the 6 blocks around a block
     * @param isLeaf true for a natural leaf block
     */
    public static <T> List<T> connectedLeaves(
            T start, Function<T, List<T>> neighbors, Predicate<T> isLeaf, int maxDepth, int cap) {
        List<T> found = new ArrayList<>();
        Set<T> seen = new HashSet<>();
        seen.add(start);
        Deque<T> queue = new ArrayDeque<>();
        Deque<Integer> depths = new ArrayDeque<>();
        queue.add(start);
        depths.add(0);
        while (!queue.isEmpty() && found.size() < cap) {
            T current = queue.poll();
            int depth = depths.poll();
            if (depth >= maxDepth) {
                continue;
            }
            for (T next : neighbors.apply(current)) {
                if (seen.add(next) && isLeaf.test(next)) {
                    found.add(next);
                    if (found.size() >= cap) {
                        break;
                    }
                    queue.add(next);
                    depths.add(depth + 1);
                }
            }
        }
        return found;
    }
}
