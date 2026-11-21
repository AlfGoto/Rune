package com.alfgoto.rune.gameplay.sanctuary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SanctuariesTest {

    private Sanctuaries sanctuaries;

    @BeforeEach
    void setUp() {
        sanctuaries = new Sanctuaries(2, 3);
        sanctuaries.register("sanctuary_taiga", "taiga");
        sanctuaries.register("sanctuary_lush_caves", "lush_caves");
    }

    @Test
    void onlyTheRuneOfThePlaceCounts() {
        List<Stacking.Source> sources = List.of(new Stacking.Source("sanctuary_taiga", 3));
        assertEquals(3, sanctuaries.total(sources, Set.of("taiga")));
        assertEquals(0, sanctuaries.total(sources, Set.of("lush_caves")));
        assertEquals(0, sanctuaries.total(sources, Set.of()));
    }

    @Test
    void sourcesFromPlayerAndArmorAddUp() {
        List<Stacking.Source> sources =
                List.of(new Stacking.Source("sanctuary_taiga", 1), new Stacking.Source("sanctuary_taiga", 1));
        assertEquals(2, sanctuaries.total(sources, Set.of("taiga")));
    }

    @Test
    void ranksGiveRegenerationThenResistanceThenRegenerationTwo() {
        assertFalse(sanctuaries.levels(0).any());
        Sanctuaries.Levels rank1 = sanctuaries.levels(1);
        assertEquals(0, rank1.regeneration());
        assertFalse(rank1.resistance());
        Sanctuaries.Levels rank2 = sanctuaries.levels(2);
        assertEquals(0, rank2.regeneration());
        assertTrue(rank2.resistance());
        Sanctuaries.Levels rank3 = sanctuaries.levels(3);
        assertEquals(1, rank3.regeneration());
        assertTrue(rank3.resistance());
    }

    @Test
    void sameEffectCannotBeRegisteredTwice() {
        assertThrows(IllegalArgumentException.class, () -> sanctuaries.register("sanctuary_taiga", "taiga"));
    }
}
