package com.alfgoto.rune.visuals.caches;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
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

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ReelMenu reel(Rarity rarity, AtomicInteger given) {
        return new ReelMenu(plugin, player, rarity, List.of(), given::incrementAndGet, new Random(3));
    }

    @Test
    void theReelEndsOnTheCacheRarityAndGivesTheLootOnce() {
        for (Rarity rarity : Rarity.values()) {
            AtomicInteger given = new AtomicInteger();
            ReelMenu menu = reel(rarity, given);
            menu.start();
            assertEquals(0, given.get(), "not before the reel ends");
            server.getScheduler().performTicks(1000);
            assertEquals(1, given.get(), rarity.name());
            assertEquals(Material.NETHER_STAR, menu.getInventory().getItem(13).getType());
            assertEquals(ReelStyle.pane(rarity), menu.getInventory().getItem(0).getType());
            menu.complete();
            assertEquals(1, given.get(), "closing after the end gives nothing more");
        }
    }

    @Test
    void closingEarlyStillGivesTheLootOnce() {
        AtomicInteger given = new AtomicInteger();
        reel(Rarity.EPIC, given).start();
        server.getScheduler().performTicks(5);
        assertEquals(0, given.get());
        player.closeInventory();
        assertEquals(1, given.get());
        server.getScheduler().performTicks(1000);
        assertEquals(1, given.get());
    }

    @Test
    void theReelSlowsDown() {
        assertTrue(ReelMenu.delay(0) < ReelMenu.delay(ReelMenu.STEPS - 1));
    }
}
