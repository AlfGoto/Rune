package com.alfgoto.rune.content.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.trees.TreeScan;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

/** Boomerang and Great Fell: the Rune numbers, and the Great Fell tree scan. */
class ActiveFellingRunesTest {

    private ServerMock server;
    private RunePlugin plugin;
    private World world;
    private RuneRegistry runes;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("greatfell");
        runes = plugin.context().get(RuneRegistry.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void trunk(int x, int z, int height) {
        for (int y = 10; y < 10 + height; y++) {
            world.getBlockAt(x, y, z).setType(Material.OAK_LOG);
        }
        world.getBlockAt(x, 10 + height, z).setType(Material.OAK_LEAVES);
    }

    @Test
    void boomerangIsEpicActiveAxeRuneWith12_16_20RangeAndCooldown30To10() {
        var rune = runes.get("boomerang");
        assertEquals(Rarity.EPIC, rune.rarity());
        assertEquals(Set.of(Fit.AXE), rune.fits());
        assertTrue(rune.isActive());
        assertEquals(12, rune.effect().valueAt(1));
        assertEquals(16, rune.effect().valueAt(2));
        assertEquals(20, rune.effect().valueAt(3));
        assertEquals(30, rune.cooldownSeconds(1));
        assertEquals(10, rune.cooldownSeconds(3));
    }

    @Test
    void greatFellIsLegendaryActiveAxeRuneWithRadius6_9_12AndCooldown90To30() {
        var rune = runes.get("great_fell");
        assertEquals(Rarity.LEGENDARY, rune.rarity());
        assertEquals(Set.of(Fit.AXE), rune.fits());
        assertTrue(rune.isActive());
        assertEquals(6, rune.effect().valueAt(1));
        assertEquals(9, rune.effect().valueAt(2));
        assertEquals(12, rune.effect().valueAt(3));
        assertEquals(90, rune.cooldownSeconds(1));
        assertEquals(30, rune.cooldownSeconds(3));
    }

    @Test
    void scanForestTakesEveryTreeInRadiusWholeAndLeavesTheOthers() {
        trunk(0, 0, 4);
        trunk(4, 0, 4);
        trunk(30, 0, 4);
        TreeScan scan = plugin.context().get(TreeScan.class);
        List<Block> blocks = scan.scanForest(world.getBlockAt(2, 10, 0), 6, 200, 600);
        // two trees of 4 logs + 1 leaf; the far tree is out of range
        assertEquals(10, blocks.size());
        assertTrue(blocks.contains(world.getBlockAt(0, 10, 0)));
        assertTrue(blocks.contains(world.getBlockAt(4, 14, 0)));
    }

    @Test
    void scanForestStartsFromTheLowestLogEvenWhenOnlyTheTopIsInRadius() {
        trunk(0, 0, 10);
        TreeScan scan = plugin.context().get(TreeScan.class);
        // the center is 8 blocks above the base: only the upper logs are within radius 3
        List<Block> blocks = scan.scanForest(world.getBlockAt(0, 18, 0), 3, 200, 600);
        assertEquals(11, blocks.size());
        assertTrue(blocks.contains(world.getBlockAt(0, 10, 0)));
    }

    @Test
    void scanForestStopsAtMaxBlocks() {
        trunk(0, 0, 20);
        TreeScan scan = plugin.context().get(TreeScan.class);
        assertEquals(5, scan.scanForest(world.getBlockAt(0, 10, 0), 6, 200, 5).size());
    }
}
