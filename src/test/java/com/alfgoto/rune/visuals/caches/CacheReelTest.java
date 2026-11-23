package com.alfgoto.rune.visuals.caches;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

    private static Drop drop(Rarity rarity) {
        return new Drop() {
            @Override
            public Component describe() {
                return Component.text("drop");
            }

            @Override
            public Rarity rarity() {
                return rarity;
            }

            @Override
            public void give(Player p) {}
        };
    }

    private ReelMenu reel(List<Drop> drops, AtomicInteger given) {
        return new ReelMenu(plugin, player, drops, given::incrementAndGet, new Random(3));
    }

    @Test
    void theReelLandsOnTheBestDropRarityAndGivesTheLootOnce() {
        for (Rarity rarity : Rarity.values()) {
            AtomicInteger given = new AtomicInteger();
            ReelMenu menu = reel(List.of(drop(Rarity.COMMON), drop(rarity), drop(null)), given);
            assertEquals(rarity, menu.landed());
            menu.start();
            assertEquals(0, given.get(), "not before the reveal");
            server.getScheduler().performTicks(1000);
            assertEquals(1, given.get(), rarity.name());
            // final state: the bar above the middle Drop (3 Drops: slots 3..5 of the middle row)
            assertEquals(Material.PAPER, menu.getInventory().getItem(13).getType());
            assertEquals(ReelStyle.pane(rarity), menu.getInventory().getItem(4).getType());
            menu.complete();
            assertEquals(1, given.get(), "closing after the end gives nothing more");
        }
    }

    @Test
    void noRarityMeansCommon() {
        assertEquals(Rarity.COMMON, ReelStyle.landed(List.of(drop(null))));
        assertEquals(Rarity.COMMON, ReelStyle.landed(List.of()));
    }

    @Test
    void theReelPauseShowsThePaneOfTheLandedRarityUnderThePointer() {
        ReelMenu menu = reel(List.of(drop(Rarity.EPIC)), new AtomicInteger());
        menu.start();
        server.getScheduler().performTicks(50);
        assertEquals(Material.HOPPER, menu.getInventory().getItem(4).getType());
        assertEquals(ReelStyle.pane(Rarity.EPIC), menu.getInventory().getItem(13).getType());
        assertEquals(Material.BLACK_STAINED_GLASS_PANE, menu.getInventory().getItem(0).getType());
    }

    @Test
    void closingEarlyStillGivesTheLootOnce() {
        AtomicInteger given = new AtomicInteger();
        reel(List.of(drop(Rarity.EPIC)), given).start();
        server.getScheduler().performTicks(5);
        assertEquals(0, given.get());
        player.closeInventory();
        assertEquals(1, given.get());
        server.getScheduler().performTicks(1000);
        assertEquals(1, given.get());
    }

    @Test
    void closingDuringThePulseGivesNothingMore() {
        AtomicInteger given = new AtomicInteger();
        ReelMenu menu = reel(List.of(drop(Rarity.LEGENDARY)), given);
        menu.start();
        int total = 0;
        for (int i = 0; i < ReelMenu.STEPS; i++) {
            total += ReelMenu.delay(i);
        }
        server.getScheduler().performTicks(total + ReelMenu.LAND_PAUSE + 1);
        assertEquals(1, given.get(), "given at the reveal, before the pulse ends");
        player.closeInventory();
        server.getScheduler().performTicks(1000);
        assertEquals(1, given.get());
    }

    @Test
    void theReelTakesAboutTwoAndAHalfSeconds() {
        int total = 0;
        for (int step = 0; step < ReelMenu.STEPS; step++) {
            total += ReelMenu.delay(step);
        }
        assertTrue(Math.abs(total - 50) <= 2, "total ticks " + total);
    }

    @Test
    void theReelSlowsDownAndEndsClearlySlow() {
        for (int step = 1; step < ReelMenu.STEPS; step++) {
            assertTrue(ReelMenu.delay(step - 1) <= ReelMenu.delay(step), "never speeds up at " + step);
        }
        assertEquals(1, ReelMenu.delay(0));
        assertTrue(ReelMenu.delay(ReelMenu.STEPS - 1) >= 4, "the last move is slow");
        assertTrue(ReelMenu.delay(ReelMenu.STEPS - 4) >= 3, "the last 4 moves are slow");
    }

    @Test
    void fillerPanesAreWeightedTowardsCommon() {
        Random random = new Random(1);
        int[] counts = new int[Rarity.values().length];
        for (int i = 0; i < 10_000; i++) {
            counts[ReelStyle.weighted(random).ordinal()]++;
        }
        for (int i = 1; i < counts.length; i++) {
            assertTrue(counts[i - 1] > counts[i], "rarer Rarities are less common at " + i);
        }
        assertNotEquals(0, counts[Rarity.LEGENDARY.ordinal()]);
    }

    @Test
    void aTooBigLootShowsTheNetherStar() {
        AtomicInteger given = new AtomicInteger();
        List<Drop> ten = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> drop(Rarity.COMMON))
                .toList();
        ReelMenu menu = reel(ten, given);
        menu.start();
        server.getScheduler().performTicks(1000);
        assertEquals(Material.NETHER_STAR, menu.getInventory().getItem(13).getType());
        assertEquals(1, given.get());
    }
}
