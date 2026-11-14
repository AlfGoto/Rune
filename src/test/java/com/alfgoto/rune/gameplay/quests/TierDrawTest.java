package com.alfgoto.rune.gameplay.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class TierDrawTest {

    private final TierDraw draw = new TierDraw(RuneConfig.defaults());

    @Test
    void epicNeedsLevel15AndLegendaryLevel25() {
        assertEquals(0, draw.weights(14)[Rarity.EPIC.ordinal()]);
        assertTrue(draw.weights(15)[Rarity.EPIC.ordinal()] > 0);
        assertEquals(0, draw.weights(24)[Rarity.LEGENDARY.ordinal()]);
        assertTrue(draw.weights(25)[Rarity.LEGENDARY.ordinal()] > 0);
    }

    @Test
    void neverDrawsGatedTiers() {
        Random random = new Random(1);
        for (int i = 0; i < 5000; i++) {
            Rarity tier = draw.draw(1, random);
            assertTrue(tier.ordinal() <= Rarity.RARE.ordinal(), "drew " + tier + " at level 1");
        }
        for (int i = 0; i < 5000; i++) {
            assertTrue(draw.draw(20, random) != Rarity.LEGENDARY);
        }
    }

    @Test
    void higherLevelsLeanTowardsHardTiers() {
        assertTrue(hardShare(60) > hardShare(5), "level 60 should draw more Epic/Legendary than level 5");
        assertTrue(hardShare(60) > 0.2);
    }

    @Test
    void drawFollowsWeights() {
        Random random = new Random(42);
        Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
        int n = 100_000;
        for (int i = 0; i < n; i++) {
            counts.merge(draw.draw(1, random), 1, Integer::sum);
        }
        double[] w = draw.weights(1);
        double total = w[0] + w[1] + w[2];
        assertEquals(w[0] / total, counts.get(Rarity.COMMON) / (double) n, 0.01);
        assertEquals(w[2] / total, counts.get(Rarity.RARE) / (double) n, 0.01);
    }

    private double hardShare(int level) {
        Random random = new Random(7);
        int hard = 0;
        int n = 20_000;
        for (int i = 0; i < n; i++) {
            if (draw.draw(level, random).ordinal() >= Rarity.EPIC.ordinal()) {
                hard++;
            }
        }
        return hard / (double) n;
    }
}
