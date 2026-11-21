package com.alfgoto.rune.gameplay.woodcutting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class RhythmTest {

    private final AtomicLong now = new AtomicLong(1_000);
    private final Rhythm rhythm = new Rhythm(10, 30_000, now::get);
    private final UUID player = UUID.randomUUID();

    @Test
    void eachFelledTreeAddsACharge() {
        assertEquals(0, rhythm.charges(player));
        assertEquals(1, rhythm.add(player));
        assertEquals(2, rhythm.add(player));
        assertEquals(0.2, rhythm.fraction(player), 1e-9);
    }

    @Test
    void chargesStopAtTheMax() {
        for (int i = 0; i < 15; i++) {
            rhythm.add(player);
        }
        assertEquals(10, rhythm.charges(player));
        assertEquals(1.0, rhythm.fraction(player), 1e-9);
    }

    @Test
    void everyChargeIsLostAfterThirtySecondsWithoutATree() {
        rhythm.add(player);
        rhythm.add(player);
        now.addAndGet(29_999);
        assertEquals(2, rhythm.charges(player));
        now.addAndGet(1);
        assertEquals(0, rhythm.charges(player));
        assertEquals(1, rhythm.add(player), "starts again from 1");
    }

    @Test
    void aNewTreeRestartsTheTimer() {
        rhythm.add(player);
        now.addAndGet(20_000);
        rhythm.add(player);
        now.addAndGet(20_000);
        assertEquals(2, rhythm.charges(player));
    }

    @Test
    void playersAreIndependentAndResetWorks() {
        UUID other = UUID.randomUUID();
        rhythm.add(player);
        rhythm.add(other);
        rhythm.reset(player);
        assertEquals(0, rhythm.charges(player));
        assertEquals(1, rhythm.charges(other));
    }

    @Test
    void badNumbersAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Rhythm(0, 1000, now::get));
        assertThrows(IllegalArgumentException.class, () -> new Rhythm(10, 0, now::get));
    }
}
