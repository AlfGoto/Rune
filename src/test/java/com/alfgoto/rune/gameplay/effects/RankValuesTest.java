package com.alfgoto.rune.gameplay.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class RankValuesTest {

    @Test
    void growsByTheStepEachRank() {
        RankValues values = RankValues.of(5, 5);
        assertEquals(5, values.at(1));
        assertEquals(10, values.at(2));
        assertEquals(15, values.at(3));
    }

    @Test
    void withoutMaxRankKeepsGrowing() {
        RankValues values = RankValues.of(5, 5);
        assertEquals(OptionalInt.empty(), values.maxRank());
        assertTrue(values.allows(1000));
        assertEquals(5000, values.at(1000));
    }

    @Test
    void zeroGrowthGivesTheSameValueAtEveryRank() {
        RankValues values = RankValues.of(0.5, 0);
        assertEquals(0.5, values.at(1));
        assertEquals(0.5, values.at(40));
    }

    @Test
    void maxRankIsCheckedByAllows() {
        RankValues values = RankValues.of(5, 5, 3);
        assertEquals(OptionalInt.of(3), values.maxRank());
        assertTrue(values.allows(3));
        assertFalse(values.allows(4));
        assertFalse(values.allows(0));
    }

    @Test
    void rankAboveMaxRankNeverThrowsAndUsesTheMaxRankValue() {
        RankValues values = RankValues.of(5, 5, 3);
        assertEquals(15, values.at(4));
        assertEquals(15, values.at(99));
    }

    @Test
    void rankBelowOneIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, 2).at(0));
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, 0).at(-1));
    }

    @Test
    void rejectsBadDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, 1, 0));
    }
}
