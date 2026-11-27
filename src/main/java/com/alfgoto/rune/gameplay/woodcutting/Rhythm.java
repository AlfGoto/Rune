package com.alfgoto.rune.gameplay.woodcutting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Rhythm charges (docs/jobs/woodcutting.md): each Felled Tree adds one charge, up to the most charges. Every
 * charge is lost after the reset time without a Felled Tree. Charges live in memory only. A charge older
 * than the reset time counts as lost even if nobody cleared it yet.
 */
public final class Rhythm {

    private record State(int charges, long lastAt) {}

    private final int maxCharges;
    private final long resetMillis;
    private final LongSupplier clock;
    private final Map<UUID, State> states = new HashMap<>();

    public Rhythm(int maxCharges, long resetMillis, LongSupplier clock) {
        if (maxCharges < 1) {
            throw new IllegalArgumentException("Max charges must be at least 1: " + maxCharges);
        }
        if (resetMillis < 1) {
            throw new IllegalArgumentException("Reset time must be at least 1 ms: " + resetMillis);
        }
        this.maxCharges = maxCharges;
        this.resetMillis = resetMillis;
        this.clock = clock;
    }

    public int maxCharges() {
        return maxCharges;
    }

    /** The charges of the player now (0 when they expired). */
    public int charges(UUID player) {
        State state = states.get(player);
        if (state == null) {
            return 0;
        }
        if (clock.getAsLong() - state.lastAt() >= resetMillis) {
            states.remove(player);
            return 0;
        }
        return state.charges();
    }

    /** Adds a charge for a Felled Tree and restarts the reset time. Returns the charges now. */
    public int add(UUID player) {
        int next = Math.min(maxCharges, charges(player) + 1);
        states.put(player, new State(next, clock.getAsLong()));
        return next;
    }

    /** Loses every charge of the player. */
    public void reset(UUID player) {
        states.remove(player);
    }

    /** The charges as a number from 0 to 1 (the rhythm Scale). */
    public double fraction(UUID player) {
        return (double) charges(player) / maxCharges;
    }
}
