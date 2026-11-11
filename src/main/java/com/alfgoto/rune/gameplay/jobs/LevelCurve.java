package com.alfgoto.rune.gameplay.jobs;

/**
 * A level curve: XP from level L to level L+1 = {@code base × L^exponent}. Levels start at 1 and
 * are read from XP up to {@link #MAX_LEVEL}. Used for Job Levels and for Account Levels, each with its own numbers from config.
 */
public record LevelCurve(double base, double exponent) {

    /** Highest level {@link #levelForXp} returns: keeps the loop short whatever the XP. */
    public static final int MAX_LEVEL = 10_000;

    public LevelCurve {
        if (!(base > 0) || !(exponent > 0) || !Double.isFinite(base) || !Double.isFinite(exponent)) {
            throw new IllegalArgumentException("Curve base and exponent must be finite and greater than 0");
        }
    }

    /** XP needed to go from {@code level} to {@code level + 1}. */
    public double xpToNextLevel(int level) {
        requireLevel(level);
        return base * Math.pow(level, exponent);
    }

    /** Total XP at which {@code level} is reached (0 for level 1). */
    public double totalXpForLevel(int level) {
        requireLevel(level);
        double total = 0;
        for (int l = 1; l < level; l++) {
            total += xpToNextLevel(l);
        }
        return total;
    }

    /** The level reached with {@code totalXp} (at least 1). */
    public int levelForXp(double totalXp) {
        int level = 1;
        double reached = 0;
        while (level < MAX_LEVEL && reached +xpToNextLevel(level) <= totalXp) {
            reached += xpToNextLevel(level);
            level++;
        }
        return level;
    }

    private static void requireLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be at least 1, got " + level);
        }
    }
}
