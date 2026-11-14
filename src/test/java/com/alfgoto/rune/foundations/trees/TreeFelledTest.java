package com.alfgoto.rune.foundations.trees;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.PdcBlockTracker;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.TreeFelled;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class TreeFelledTest {

    private ServerMock server;
    private World world;
    private PlayerMock player;
    private BlockTracker tracker;
    private TreeScan scan;
    private final List<TreeFelled> felled = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("w");
        player = server.addPlayer();
        tracker = new PdcBlockTracker("rune");
        scan = new TreeScan(tracker);
        Plugin plugin = MockBukkit.createMockPlugin();
        server.getPluginManager().registerEvents(new TreeFelledListener(server, scan), plugin);
        server.getPluginManager().registerEvents(new Collector(), plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Block at(int x, int y, int z, Material type) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(type);
        return block;
    }

    /** A 4-log oak trunk at x=0, z=0 from y=64 to y=67. */
    private List<Block> trunk() {
        List<Block> logs = new ArrayList<>();
        for (int y = 64; y <= 67; y++) {
            logs.add(at(0, y, 0, Material.OAK_LOG));
        }
        return logs;
    }

    /** Breaks like Paper does: the Rune event runs while the block is still there, then the block goes. */
    private void breakBlock(Block block) {
        server.getPluginManager()
                .callEvent(new RuneBlockBreakEvent(player, block, BreakCause.TOOL, new ItemStack(Material.IRON_AXE)));
        block.setType(Material.AIR);
    }

    @Test
    void breakingTheLastLogFiresOnce() {
        List<Block> logs = trunk();
        for (Block log : logs) {
            breakBlock(log);
        }
        assertEquals(1, felled.size());
        assertEquals(player, felled.get(0).player());
        assertEquals(logs.get(3), felled.get(0).lastLog());
    }

    @Test
    void cuttingOnlyTheTopDoesNotFell() {
        List<Block> logs = trunk();
        breakBlock(logs.get(3));
        breakBlock(logs.get(2));
        assertTrue(felled.isEmpty());
    }

    @Test
    void cuttingTheBottomFirstDoesNotFellUntilTheTopGoes() {
        List<Block> logs = trunk();
        breakBlock(logs.get(0));
        breakBlock(logs.get(1));
        breakBlock(logs.get(2));
        assertTrue(felled.isEmpty());
        breakBlock(logs.get(3));
        assertEquals(1, felled.size());
    }

    @Test
    void aSingleLogIsATree() {
        breakBlock(at(5, 64, 5, Material.BIRCH_LOG));
        assertEquals(1, felled.size());
    }

    @Test
    void brokenNonLogDoesNothing() {
        breakBlock(at(0, 64, 0, Material.STONE));
        breakBlock(at(1, 64, 0, Material.OAK_LEAVES));
        assertTrue(felled.isEmpty());
    }

    @Test
    void placedLogIsNotATree() {
        Block log = at(0, 64, 0, Material.OAK_LOG);
        tracker.add(log, BlockTag.PLACED);
        breakBlock(log);
        assertTrue(felled.isEmpty());
    }

    @Test
    void placedLogsDoNotKeepATreeAlive() {
        List<Block> logs = trunk();
        Block placed = at(0, 68, 0, Material.OAK_LOG);
        tracker.add(placed, BlockTag.PLACED);
        for (Block log : logs) {
            breakBlock(log);
        }
        assertEquals(1, felled.size());
    }

    @Test
    void otherWoodTypeIsAnotherTree() {
        Block oak = at(0, 64, 0, Material.OAK_LOG);
        at(0, 65, 0, Material.SPRUCE_LOG);
        breakBlock(oak);
        assertEquals(1, felled.size());
    }

    @Test
    void strippedAndWoodBlocksAreTheSameWoodType() {
        Block log = at(0, 64, 0, Material.OAK_LOG);
        at(0, 65, 0, Material.STRIPPED_OAK_LOG);
        at(0, 66, 0, Material.OAK_WOOD);
        breakBlock(log);
        assertTrue(felled.isEmpty());
    }

    @Test
    void diagonalBranchKeepsTheTreeAlive() {
        Block trunk = at(0, 64, 0, Material.DARK_OAK_LOG);
        at(1, 65, 1, Material.DARK_OAK_LOG);
        breakBlock(trunk);
        assertTrue(felled.isEmpty());
    }

    @Test
    void netherStemsAreLogs() {
        Block stem = at(0, 64, 0, Material.CRIMSON_STEM);
        at(0, 65, 0, Material.WARPED_STEM);
        breakBlock(stem);
        assertEquals(1, felled.size());
    }

    @Test
    void scanAtOrAboveSkipsLogsBelow() {
        List<Block> logs = trunk();
        TreeScan.Result above = scan.scan(logs.get(1), 100, true);
        assertEquals(List.of(logs.get(2), logs.get(3)), above.logs());
        TreeScan.Result all = scan.scan(logs.get(1), 100, false);
        assertEquals(3, all.logs().size());
    }

    @Test
    void scanNeverReturnsTheOrigin() {
        List<Block> logs = trunk();
        assertFalse(scan.scan(logs.get(0), 100, true).logs().contains(logs.get(0)));
    }

    @Test
    void scanAddsTouchingCanopyButNotPlacedCanopy() {
        List<Block> logs = trunk();
        Block leaf = at(1, 67, 0, Material.OAK_LEAVES);
        Block wart = at(-1, 66, 0, Material.NETHER_WART_BLOCK);
        Block placedLeaf = at(0, 68, 0, Material.OAK_LEAVES);
        tracker.add(placedLeaf, BlockTag.PLACED);
        Block far = at(5, 67, 5, Material.OAK_LEAVES);
        TreeScan.Result result = scan.scan(logs.get(0), 100, true);
        assertTrue(result.canopy().contains(leaf));
        assertTrue(result.canopy().contains(wart));
        assertFalse(result.canopy().contains(placedLeaf));
        assertFalse(result.canopy().contains(far));
    }

    @Test
    void capCountsLogsAndCanopyTogether() {
        List<Block> logs = trunk();
        at(1, 64, 0, Material.OAK_LEAVES);
        at(1, 65, 0, Material.OAK_LEAVES);
        TreeScan.Result result = scan.scan(logs.get(0), 4, true);
        assertEquals(3, result.logs().size());
        assertEquals(1, result.canopy().size());
        assertEquals(1, scan.scan(logs.get(0), 1, true).logs().size());
    }

    @Test
    void woodTypeNames() {
        assertEquals("oak", TreeScan.woodType(Material.OAK_LOG));
        assertEquals("oak", TreeScan.woodType(Material.STRIPPED_OAK_WOOD));
        assertEquals("crimson", TreeScan.woodType(Material.CRIMSON_HYPHAE));
        assertEquals("warped", TreeScan.woodType(Material.STRIPPED_WARPED_STEM));
        assertEquals("pale_oak", TreeScan.woodType(Material.PALE_OAK_LOG));
        assertNull(TreeScan.woodType(Material.STONE));
        assertNull(TreeScan.woodType(Material.OAK_LEAVES));
    }

    private final class Collector implements Listener {
        @EventHandler
        public void onFelled(TreeFelled event) {
            felled.add(event);
        }
    }
}
