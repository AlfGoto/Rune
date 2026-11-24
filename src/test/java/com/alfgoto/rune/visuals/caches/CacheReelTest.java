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
import org.bukkit.inventory.ItemStack;
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

    /** One filler material per Rarity, so the tests can tell which Rarity spins past. */
    private static ItemStack filler(Rarity rarity) {
        return new ItemStack(FILLERS[rarity.ordinal()]);
    }

    private static final Material[] FILLERS = {
        Material.WHITE_WOOL, Material.LIME_WOOL, Material.LIGHT_BLUE_WOOL, Material.MAGENTA_WOOL, Material.ORANGE_WOOL
    };

    private ReelMenu reel(List<Drop> drops, AtomicInteger given) {
        return new ReelMenu(
                plugin, player, Rarity.RARE, drops, given::incrementAndGet, new Random(3), CacheReelTest::filler);
    }

    private static int spinTicks() {
        int total = 0;
        for (int i = 0; i < ReelMenu.STEPS; i++) {
            total += ReelMenu.delay(i);
        }
        return total;
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
            // final state: the 3 Drops centred in the reel row (slots 21..23), the rest empty
            for (int slot = 21; slot <= 23; slot++) {
                assertEquals(Material.PAPER, menu.getInventory().getItem(slot).getType(), "slot " + slot);
            }
            assertNotEquals(Material.PAPER, menu.getInventory().getItem(20).getType());
            assertNotEquals(Material.PAPER, menu.getInventory().getItem(24).getType());
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
    void theReelSpinsFillersAndStopsOnTheBestDropWithANearMiss() {
        ReelMenu menu = reel(List.of(drop(Rarity.COMMON), drop(Rarity.EPIC)), new AtomicInteger());
        menu.start();
        assertTrue(java.util.Arrays.asList(FILLERS).contains(menu.getInventory().getItem(ReelMenu.CENTER).getType()),
                "filler items spin before the end");
        server.getScheduler().performTicks(spinTicks() + 1);
        ItemStack landing = menu.getInventory().getItem(ReelMenu.CENTER);
        assertEquals(Material.PAPER, landing.getType(), "the best Drop is under the pointer");
        assertTrue(landing.getItemMeta().getEnchantmentGlintOverride());
        Material left = menu.getInventory().getItem(ReelMenu.CENTER - 1).getType();
        Material right = menu.getInventory().getItem(ReelMenu.CENTER + 1).getType();
        assertTrue(left == Material.ORANGE_WOOL || right == Material.ORANGE_WOOL,
                "a Legendary sits next to the Epic landing");
    }

    @Test
    void theRevealRedrawsTheTitleWithoutClosingTheReel() {
        AtomicInteger given = new AtomicInteger();
        ReelMenu menu = reel(List.of(drop(Rarity.RARE)), given);
        menu.start();
        server.getScheduler().performTicks(spinTicks() + ReelMenu.LAND_PAUSE + 1);
        assertEquals(1, given.get());
        assertEquals(menu, player.getOpenInventory().getTopInventory().getHolder(), "still open after the reveal");
        server.getScheduler().performTicks(40);
        assertEquals(1, given.get(), "the Legendary-style sounds after the reveal give nothing more");
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
    void closingAfterTheRevealGivesNothingMore() {
        AtomicInteger given = new AtomicInteger();
        ReelMenu menu = reel(List.of(drop(Rarity.LEGENDARY)), given);
        menu.start();
        server.getScheduler().performTicks(spinTicks() + ReelMenu.LAND_PAUSE + 1);
        assertEquals(1, given.get(), "given at the reveal");
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
    void fillerItemsAreWeightedTowardsCommon() {
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
        assertEquals(Material.NETHER_STAR, menu.getInventory().getItem(ReelMenu.CENTER).getType());
        assertEquals(1, given.get());
    }
}
