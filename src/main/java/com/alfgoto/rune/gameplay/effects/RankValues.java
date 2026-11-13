package com.alfgoto.rune.gameplay.effects;

import java.util.Arrays;

/**
 * One value per Rank: index 0 is Rank 1. Ranks start at 1.
 *
 * <p>Asking for a Rank above the last value is an error, so a Rune can never silently get a value nobody
 * wrote. A single value applies to every Rank.
 */
public final class RankValues {

    private final double[] values;

    private RankValues(double[] values) {
        this.values = values;
    }

    /** One value per Rank, starting at Rank 1. At least one value. */
    public static RankValues of(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one value is needed");
        }
        for (double value : values) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException("Values must be finite numbers");
            }
        }
        return new RankValues(values.clone());
    }

    /** The same value at every Rank, for an effect that does not scale. */
    public static RankValues constant(double value) {
        return of(value);
    }

    /** Number of values given. */
    public int size() {
        return values.length;
    }

    /** The value at {@code rank}. A single value (see {@link #constant}) applies to every Rank. */
    public double at(int rank) {
        if (rank < 1) {
            throw new IllegalArgumentException("Rank starts at 1, got " + rank);
        }
        if (values.length == 1) {
            return values[0];
        }
        if (rank > values.length) {
            throw new IllegalArgumentException("No value for Rank " + rank + " (defined: " + values.length + ")");
        }
        return values[rank - 1];
    }

    @Override
    public String toString() {
        return "RankValues" + Arrays.toString(values);
    }
}
