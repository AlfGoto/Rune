package com.alfgoto.rune.gameplay.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.gameplay.effects.Stacking.Source;
import java.util.List;
import org.junit.jupiter.api.Test;

class StackingTest {

    private static Effect<Object> effect(String id) {
        return Effect.builder(id, new Trigger<>("test", Object.class, e -> null)).does(ctx -> {});
    }

    private static Source source(String id, double base, double perRank, int rank) {
        return new Source(effect(id).with(base, perRank), rank);
    }

    @Test
    void traitAndRunesAddUp() {
        // Trait: smelt 50. Rune rank 2: 5 + 5 = 10. Rune rank 1: 5.
        List<Source> sources = List.of(
                source("smelt", 50, 0, 1), source("smelt", 5, 5, 2), source("smelt", 5, 5, 1));
        assertEquals(65, Stacking.total("smelt", sources));
    }

    @Test
    void otherEffectsAreNotCounted() {
        List<Source> sources = List.of(source("speed", 10, 0, 1), source("smelt", 5, 0, 1));
        assertEquals(5, Stacking.total("smelt", sources));
        assertEquals(0, Stacking.total("haste", sources));
    }

    @Test
    void chanceAbove100RollsAgain() {
        // Smelt 150%: always one smelt, 50% chance of a second.
        assertEquals(2, Stacking.rolls(150, () -> 0.49));
        assertEquals(1, Stacking.rolls(150, () -> 0.5));
        assertEquals(1, Stacking.rolls(150, () -> 0.99));
    }

    @Test
    void exactMultipleOf100NeedsNoRoll() {
        assertEquals(2, Stacking.rolls(200, () -> {
            throw new AssertionError("no roll expected");
        }));
        assertEquals(1, Stacking.rolls(100, () -> 0.999));
    }

    @Test
    void lowChanceRollsOnce() {
        assertEquals(1, Stacking.rolls(5, () -> 0.04));
        assertEquals(0, Stacking.rolls(5, () -> 0.05));
        assertEquals(0, Stacking.rolls(0, () -> 0));
        assertEquals(0, Stacking.rolls(-10, () -> 0));
    }

    @Test
    void silkTouchTurnsOffFortuneSmeltAndOreBloom() {
        List<Source> sources = List.of(
                source("silk_touch", 1, 0, 1),
                source("fortune", 20, 0, 1),
                source("smelt", 50, 0, 1),
                source("ore_bloom", 10, 0, 1),
                source("speed", 10, 0, 1));
        assertEquals(0, Stacking.total("fortune", sources));
        assertEquals(0, Stacking.total("smelt", sources));
        assertEquals(0, Stacking.total("ore_bloom", sources));
        assertTrue(Stacking.isDisabled("smelt", sources));
        // Other effects keep working.
        assertEquals(10, Stacking.total("speed", sources));
        assertFalse(Stacking.isDisabled("speed", sources));
    }

    @Test
    void withoutSilkTouchNothingIsDisabled() {
        List<Source> sources = List.of(source("fortune", 20, 0, 1));
        assertEquals(20, Stacking.total("fortune", sources));
    }
}
