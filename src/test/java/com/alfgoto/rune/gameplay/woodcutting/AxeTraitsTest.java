package com.alfgoto.rune.gameplay.woodcutting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AxeTraitsTest {

    @Test
    void climbSpeedAddsThePercentToTheVanillaSpeed() {
        assertEquals(0.2, AxeTraits.climbSpeed(0), 1e-9);
        assertEquals(0.3, AxeTraits.climbSpeed(50), 1e-9);
        assertEquals(0.2, AxeTraits.climbSpeed(-30), 1e-9);
    }

    @Test
    void powderSnowCompensationCancelsTheNinetyPercentSlowdown() {
        assertEquals(1.0, 0.9 * (1 + AxeTraits.powderSnowCompensation()), 1e-9);
    }

    @Test
    void teleportCooldownLastsUntilTheDelayPasses() {
        assertTrue(AxeTraits.onCooldown(1_000, 5_000, 8_000));
        assertFalse(AxeTraits.onCooldown(1_000, 9_000, 8_000));
    }

    @Test
    void teleportOffsetStaysInsideTheRangeAndAwayFromTheStart() {
        for (double a = 0; a < 1; a += 0.1) {
            for (double d = 0; d < 1; d += 0.1) {
                double[] offset = AxeTraits.teleportOffset(8, a, d);
                double distance = Math.hypot(offset[0], offset[1]);
                assertTrue(distance >= 1 - 1e-9 && distance <= 8 + 1e-9);
            }
        }
    }

    /** A line of blocks: block 0 is the log, the others are leaves unless the test says otherwise. */
    private static List<Integer> neighbors(int block) {
        return List.of(block - 1, block + 1);
    }

    @Test
    void connectedLeavesFollowsTheChainAndStopsAtAGap() {
        // Block 5 is air: 6 and beyond are not linked to the log.
        List<Integer> found =
                AxeTraits.connectedLeaves(0, AxeTraitsTest::neighbors, b -> b >= 1 && b != 5, 7, 100);
        assertEquals(List.of(1, 2, 3, 4), found);
    }

    @Test
    void connectedLeavesStopsAtTheReachAndTheCap() {
        assertEquals(List.of(1, 2, 3), AxeTraits.connectedLeaves(0, AxeTraitsTest::neighbors, b -> b >= 1, 3, 100));
        assertEquals(List.of(1, 2), AxeTraits.connectedLeaves(0, AxeTraitsTest::neighbors, b -> b >= 1, 7, 2));
    }
}
