package com.alfgoto.rune.gameplay.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RankValuesTest {

    @Test
    void resolvesOneValuePerRank() {
        RankValues values = RankValues.of(8, 16, 32);
        assertEquals(8, values.at(1));
        assertEquals(16, values.at(2));
        assertEquals(32, values.at(3));
        assertEquals(3, values.size());
    }

    @Test
    void singleValueAppliesToEveryRank() {
        RankValues values = RankValues.constant(0.5);
        assertEquals(0.5, values.at(1));
        assertEquals(0.5, values.at(40));
    }

    @Test
    void rankAboveLastValueIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, 2).at(3));
    }

    @Test
    void rankBelowOneIsAnError() {
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, 2).at(0));
        assertThrows(IllegalArgumentException.class, () -> RankValues.constant(1).at(-1));
    }

    @Test
    void needsAtLeastOneFiniteValue() {
        assertThrows(IllegalArgumentException.class, RankValues::of);
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> RankValues.of(Double.POSITIVE_INFINITY));
    }

    @Test
    void copiesTheInputArray() {
        double[] input = {1, 2, 3};
        RankValues values = RankValues.of(input);
        input[0] = 99;
        assertEquals(1, values.at(1));
    }
}
