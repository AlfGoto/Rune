package com.alfgoto.rune.gameplay.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LevelCurveTest {

    private final LevelCurve job = new LevelCurve(50, 1.3);
    private final LevelCurve account = new LevelCurve(200, 1.3);

    @Test
    void jobCurveIs50TimesLevelPower1_3() {
        assertEquals(50.0, job.xpToNextLevel(1), 1e-9);
        assertEquals(50 * Math.pow(10, 1.3), job.xpToNextLevel(10), 1e-9);
    }

    @Test
    void accountCurveIs200TimesLevelPower1_3() {
        assertEquals(200.0, account.xpToNextLevel(1), 1e-9);
        assertEquals(200 * Math.pow(10, 1.3), account.xpToNextLevel(10), 1e-9);
    }

    @Test
    void totalXpMatchesTheMiningDocTable() {
        assertEquals(690, job.totalXpForLevel(5), 30);
        assertEquals(3850, job.totalXpForLevel(10), 100);
        assertEquals(10200, job.totalXpForLevel(15), 300);
        assertEquals(20000, job.totalXpForLevel(20), 600);
        assertEquals(34000, job.totalXpForLevel(25), 1000);
    }

    @Test
    void levelStartsAtOneWithNoXp() {
        assertEquals(0, job.totalXpForLevel(1));
        assertEquals(1, job.levelForXp(0));
        assertEquals(1, job.levelForXp(49.9));
        assertEquals(2, job.levelForXp(50));
    }

    @Test
    void levelForXpInvertsTotalXpForLevel() {
        for (int level = 1; level <= 200; level++) {
            double total = job.totalXpForLevel(level);
            assertEquals(level, job.levelForXp(total));
            assertEquals(level - 1 < 1 ? 1 : level - 1, job.levelForXp(Math.max(0, total - 0.001)));
        }
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> job.xpToNextLevel(0));
        assertThrows(IllegalArgumentException.class, () -> new LevelCurve(0, 1.3));
        assertThrows(IllegalArgumentException.class, () -> new LevelCurve(50, Double.NaN));
    }
}
