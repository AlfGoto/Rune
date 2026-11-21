package com.alfgoto.rune.gameplay.caches;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.config.CacheConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CacheDropsTest {

    private final CacheDrops drops = new CacheDrops(CacheConfig.defaults());

    @Test
    void chanceIsXpTimesRate() {
        assertEquals(0.0001, drops.chance(0.2), 1e-12); // stone: 1 in 10,000
        assertEquals(0.015, drops.chance(30), 1e-12); // diamond ore: 1.5%
        assertEquals(0.02, drops.chance(40), 1e-12); // emerald ore: 2%
    }

    @Test
    void noXpMeansNoChanceAndChanceIsCapped() {
        assertEquals(0, drops.chance(0));
        assertEquals(0, drops.chance(-3));
        assertEquals(1, new CacheDrops(new CacheConfig(100, CacheConfig.defaults().milestones(), 5, 25)).chance(8));
    }

    @Test
    void emptyTableMeansNoMilestones() {
        CacheDrops none = new CacheDrops(new CacheConfig(0.05, java.util.Map.of(), 5, 25));
        assertTrue(none.milestone(5).isEmpty());
        assertTrue(none.milestone(50).isEmpty());
    }

    @Test
    void tableLevels() {
        assertEquals(Optional.of(Rarity.COMMON), drops.milestone(5));
        assertEquals(Optional.of(Rarity.UNCOMMON), drops.milestone(10));
        assertEquals(Optional.of(Rarity.RARE), drops.milestone(15));
        assertEquals(Optional.of(Rarity.RARE), drops.milestone(20));
        assertEquals(Optional.of(Rarity.EPIC), drops.milestone(25));
        assertEquals(Optional.of(Rarity.EPIC), drops.milestone(30));
    }

    @Test
    void levelsBetweenMilestonesGiveNothing() {
        for (int level : new int[] {0, 1, 4, 6, 9, 11, 19, 21, 29, 31, 49}) {
            assertTrue(drops.milestone(level).isEmpty(), "level " + level);
        }
    }

    @Test
    void aboveTheTableEpicEveryFiveAndLegendaryEveryTwentyFive() {
        assertEquals(Optional.of(Rarity.EPIC), drops.milestone(35));
        assertEquals(Optional.of(Rarity.EPIC), drops.milestone(45));
        assertEquals(Optional.of(Rarity.LEGENDARY), drops.milestone(50));
        assertEquals(Optional.of(Rarity.EPIC), drops.milestone(55));
        assertEquals(Optional.of(Rarity.LEGENDARY), drops.milestone(75));
        assertEquals(Optional.of(Rarity.LEGENDARY), drops.milestone(100));
    }
}
