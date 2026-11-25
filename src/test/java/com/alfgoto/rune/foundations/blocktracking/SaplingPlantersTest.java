package com.alfgoto.rune.foundations.blocktracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.world.WorldMock;

class SaplingPlantersTest {

    private WorldMock world;

    @BeforeEach
    void setUp() {
        world = MockBukkit.mock().addSimpleWorld("planters");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void remembersEachPlanterPerPositionAndForgetsOnClear() {
        SaplingPlanters planters = new SaplingPlanters("rune");
        Block a = world.getBlockAt(1, 64, 1);
        Block b = world.getBlockAt(2, 64, 1);
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();
        planters.set(a, alice);
        planters.set(b, bob);
        assertEquals(Optional.of(alice), planters.get(a));
        assertEquals(Optional.of(bob), planters.get(b));
        planters.clear(a);
        assertTrue(planters.get(a).isEmpty());
        assertEquals(Optional.of(bob), planters.get(b));
    }
}
