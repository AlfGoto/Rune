package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.config.RuneConfig;

/**
 * The Capacity a player has for their Runes: a base plus a set amount per Account Level, up to a cap.
 * The numbers come from the config ({@code capacity.player}).
 */
public final class PlayerCapacity {

    private final int base;
    private final int perAccountLevel;
    private final int max;

    public PlayerCapacity(RuneConfig config) {
        this.base = config.playerCapacityBase();
        this.perAccountLevel = config.playerCapacityPerLevel();
        this.max = config.playerCapacityMax();
    }

    /** The Player Capacity at {@code accountLevel}. */
    public int forAccountLevel(int accountLevel) {
        long capacity = base + (long) perAccountLevel * accountLevel;
        return (int) Math.min(max, capacity);
    }
}
