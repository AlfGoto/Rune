package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Draws a Quest Tier for a Job Level.
 *
 * <p>A Tier below its minimum Job Level (Epic 15, Legendary 25) has weight 0. Otherwise its weight is
 * {@code max(0, base + perLevel × level)}, so higher levels lean towards hard Tiers. All numbers come
 * from the config.
 */
public final class TierDraw {

    private final List<Double> minLevels;
    private final List<Double> base;
    private final List<Double> perLevel;

    public TierDraw(RuneConfig config) {
        this.minLevels = config.questTierMinLevels();
        this.base = config.questTierWeightBase();
        this.perLevel = config.questTierWeightPerLevel();
    }

    /** Weight of every Tier at {@code jobLevel}, lowest Tier first. */
    public double[] weights(int jobLevel) {
        Rarity[] tiers = Rarity.values();
        double[] weights = new double[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            if (jobLevel >= minLevels.get(i)) {
                weights[i] = Math.max(0, base.get(i) + perLevel.get(i) * jobLevel);
            }
        }
        return weights;
    }

    /** Picks a Tier. If every weight is 0 (bad config), returns Common. */
    public Rarity draw(int jobLevel, RandomGenerator random) {
        double[] weights = weights(jobLevel);
        double total = 0;
        for (double w : weights) {
            total += w;
        }
        if (total <= 0) {
            return Rarity.COMMON;
        }
        double roll = random.nextDouble() * total;
        Rarity[] tiers = Rarity.values();
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0 && weights[i] > 0) {
                return tiers[i];
            }
        }
        // Rounding edge: the last Tier that can be drawn.
        for (int i = weights.length - 1; i >= 0; i--) {
            if (weights[i] > 0) {
                return tiers[i];
            }
        }
        return Rarity.COMMON;
    }
}
