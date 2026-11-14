package com.alfgoto.rune.gameplay.jobs;

import java.util.Objects;

/** What breaking one kind of block is worth: the Job that owns the block and its base Job XP. */
public record BlockAction(Job job, double xp) {

    public BlockAction {
        Objects.requireNonNull(job, "job");
        if (!Double.isFinite(xp) || xp <= 0) {
            throw new IllegalArgumentException("Block Job XP must be greater than 0, got " + xp);
        }
    }
}
