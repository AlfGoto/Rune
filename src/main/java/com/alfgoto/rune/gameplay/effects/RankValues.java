package com.alfgoto.rune.gameplay.effects;

import java.util.OptionalInt;

/**
 * A value that grows with Rank: {@code base + perRank * (rank - 1)}. Ranks start at 1.
 *
 * <p>Example: base 5, perRank 5 gives 5, 10, 15, ... A Rune can have a Max Rank. With no Max Rank the
 * value keeps growing without limit.
 *
 * <p>The Max Rank is checked when the Rune is ranked up, with {@link #allows}. {@link #at} never throws for
 * a Rank above the Max Rank: it returns the value of the Max Rank, so a Bukkit listener cannot fail on it.
 */
public final class RankValues {

    private final double base;
    private final double perRank;
    private final int maxRank; // 0 = no Max Rank

    private RankValues(double base, double perRank, int maxRank) {
        this.base = base;
        this.perRank = perRank;
        this.maxRank = maxRank;
    }

    /** Starts at {@code base} on Rank 1 and adds {@code perRank} each Rank. No Max Rank. */
    public static RankValues of(double base, double perRank) {
        return create(base, perRank, 0);
    }

    /** Same as {@link #of(double, double)} with a Max Rank (at least 1). */
    public static RankValues of(double base, double perRank, int maxRank) {
        if (maxRank < 1) {
            throw new IllegalArgumentException("Max Rank must be at least 1, got " + maxRank);
        }
        return create(base, perRank, maxRank);
    }

    private static RankValues create(double base, double perRank, int maxRank) {
        if (!Double.isFinite(base) || !Double.isFinite(perRank)) {
            throw new IllegalArgumentException("Base and growth per Rank must be finite numbers");
        }
        return new RankValues(base, perRank, maxRank);
    }

    public double base() {
        return base;
    }

    public double perRank() {
        return perRank;
    }

    /** The Max Rank, or empty if the Rune has none. */
    public OptionalInt maxRank() {
        return maxRank == 0 ? OptionalInt.empty() : OptionalInt.of(maxRank);
    }

    /** True if {@code rank} is a valid Rank: at least 1 and not above the Max Rank. Use it when ranking up. */
    public boolean allows(int rank) {
        return rank >= 1 && (maxRank == 0 || rank <= maxRank);
    }

    /** The value at {@code rank}. A Rank above the Max Rank gives the value of the Max Rank. */
    public double at(int rank) {
        if (rank < 1) {
            throw new IllegalArgumentException("Rank starts at 1, got " + rank);
        }
        int effective = maxRank == 0 ? rank : Math.min(rank, maxRank);
        return base + perRank * (effective - 1);
    }

    @Override
    public String toString() {
        return "RankValues[base=" + base + ", perRank=" + perRank
                + (maxRank == 0 ? "" : ", maxRank=" + maxRank) + "]";
    }
}
