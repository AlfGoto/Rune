package com.alfgoto.rune.visuals.cachereel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.text.Rarity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class CacheReelTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private Caches caches;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        caches = plugin.context().get(Caches.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /** Whether the loot was given: giving it tells the player in chat. */
    private boolean given() {
        return player.nextComponentMessage() != null;
    }

    private int ticks() {
        int total = 0;
        for (int delay : CacheReel.DELAYS) {
            total += delay;
        }
        return total;
    }

    @Test
    void openingACacheShowsTheReelThenTheRewards() {
        assertTrue(caches.open(player, caches.create(Rarity.COMMON, null)));
        CacheReel reel = assertInstanceOf(CacheReel.class, player.getOpenInventory().getTopInventory().getHolder());
        assertFalse(reel.revealed());
        assertFalse(given(), "the loot waits for the reel");

        server.getScheduler().performTicks(ticks() + 1);

        assertTrue(reel.revealed());
        assertNotNull(reel.getInventory().getItem(CacheReel.CENTER));
        server.getScheduler().performTicks(Caches.REVEAL_TICKS);
        assertTrue(given(), "the loot is given when the reel ends");
    }

    @Test
    void theReelEndsBeforeTheLootIsGiven() {
        assertTrue(ticks() < Caches.REVEAL_TICKS);
    }

    @Test
    void leavingBeforeTheEndStillGivesTheLoot() {
        caches.open(player, caches.create(Rarity.COMMON, null));
        player.closeInventory();
        server.getScheduler().performTicks(Caches.REVEAL_TICKS + 1);
        assertTrue(given());
    }

    @Test
    void quittingGivesTheLootAtOnce() {
        caches.open(player, caches.create(Rarity.COMMON, null));
        player.disconnect();
        assertTrue(given());
    }
}
