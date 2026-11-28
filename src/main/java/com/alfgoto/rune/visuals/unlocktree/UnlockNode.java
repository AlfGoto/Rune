package com.alfgoto.rune.visuals.unlocktree;

import com.alfgoto.rune.gameplay.jobs.Job;
import java.util.Comparator;
import java.util.List;

/** One node of a Job's line: a Job Level that unlocks something. */
public record UnlockNode(Job job, int level, List<Unlock> unlocks) {

    public UnlockNode {
        if (unlocks.isEmpty()) {
            throw new IllegalArgumentException("A node needs at least one unlock");
        }
        unlocks = List.copyOf(unlocks);
    }

    /** The strongest unlock of the node: Blueprint, then Milestone Cache, then recipe. It gives the frame and icon. */
    public Unlock main() {
        return unlocks.stream().max(Comparator.comparingInt(u -> u.kind().ordinal())).orElseThrow();
    }

    /** The advancement id part of this node, e.g. {@code mining/l12}. */
    public String path() {
        return job.id() + "/l" + level;
    }
}
