package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.List;

/**
 * What a Rune consumes from Capacity: a base by Rarity, plus a set amount for each Rank above 1.
 * The numbers come from the config ({@code capacity.rune-cost}).
 */
public final class CapacityCost {

    private final List<Double> base;
    private final double perRank;

    public CapacityCost(RuneConfig config) {
        this.base = config.runeCostBase();
        this.perRank = config.runeCostPerRank();
    }

    /** The Capacity Cost of a Rune of this Rarity at {@code rank} (at least 1). */
    public long of(Rarity rarity, int rank) {
        if (rank < 1) {
            throw new IllegalArgumentException("Rank starts at 1, got " + rank);
        }
        return Math.round(base.get(rarity.ordinal()) + perRank * (rank - 1L));
    }
}
