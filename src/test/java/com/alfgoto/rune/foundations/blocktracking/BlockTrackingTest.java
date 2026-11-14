package com.alfgoto.rune.foundations.blocktracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class BlockTrackingTest {

    private World world;
    private BlockTracker tracker;
    private BlockTrackingListener listener;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        world = server.addSimpleWorld("w");
        tracker = new PdcBlockTracker("rune");
        listener = new BlockTrackingListener(tracker);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void addHasRemove() {
        Block block = world.getBlockAt(3, 64, -5);
        assertFalse(tracker.has(block, BlockTag.PLACED));
        tracker.add(block, BlockTag.PLACED);
        tracker.add(block, BlockTag.PLACED);
        assertTrue(tracker.has(block, BlockTag.PLACED));
        assertEquals(Set.of(BlockTag.PLACED), tracker.tags(block));
        assertFalse(tracker.has(world.getBlockAt(4, 64, -5), BlockTag.PLACED));
        tracker.remove(block, BlockTag.PLACED);
        assertFalse(tracker.has(block, BlockTag.PLACED));
        assertTrue(tracker.tags(block).isEmpty());
    }

    @Test
    void tagsLiveInChunkPdcSoTheySurviveRestart() {
        Block block = world.getBlockAt(3, 64, 5);
        tracker.add(block, BlockTag.PLACED);
        // A fresh tracker reads the same chunk data, like after a restart.
        assertTrue(new PdcBlockTracker("rune").has(block, BlockTag.PLACED));
    }

    @Test
    void clearRemovesEveryTag() {
        Block block = world.getBlockAt(0, 10, 0);
        tracker.add(block, BlockTag.PLACED);
        tracker.clear(block);
        assertFalse(tracker.has(block, BlockTag.PLACED));
    }

    private Block piston(Material type, BlockFace facing) {
        Block piston = world.getBlockAt(0, 64, 0);
        piston.setType(type);
        Piston data = (Piston) piston.getBlockData();
        data.setFacing(facing);
        piston.setBlockData(data);
        return piston;
    }

    @Test
    void pistonExtendMovesTag() {
        Block piston = piston(Material.PISTON, BlockFace.EAST);
        Block pushed = world.getBlockAt(1, 64, 0);
        tracker.add(pushed, BlockTag.PLACED);
        listener.onPistonExtend(new BlockPistonExtendEvent(piston, List.of(pushed), BlockFace.EAST));
        assertFalse(tracker.has(pushed, BlockTag.PLACED));
        assertTrue(tracker.has(world.getBlockAt(2, 64, 0), BlockTag.PLACED));
    }

    @Test
    void pistonRetractMovesTagBack() {
        Block piston = piston(Material.STICKY_PISTON, BlockFace.EAST);
        Block pulled = world.getBlockAt(2, 64, 0);
        tracker.add(pulled, BlockTag.PLACED);
        listener.onPistonRetract(new BlockPistonRetractEvent(piston, List.of(pulled), BlockFace.EAST));
        assertFalse(tracker.has(pulled, BlockTag.PLACED));
        assertTrue(tracker.has(world.getBlockAt(1, 64, 0), BlockTag.PLACED));
    }

    @Test
    void debugToolIsCalledBlocksAndNeedsABlockInView() {
        var debug = new BlocksDebug(tracker);
        assertEquals("blocks", debug.name());
        var player = MockBukkit.getMock().addPlayer();
        debug.execute(player, List.of());
        assertEquals(
                "rune.block_tracking.debug.none",
                ((net.kyori.adventure.text.TranslatableComponent) player.nextComponentMessage()).key());
    }

    private static org.bukkit.block.BlockState state(Block block, Material type) {
        org.bukkit.block.BlockState state = block.getState();
        state.setType(type);
        return state;
    }

    @Test
    void naturalTreeGrowthGetsGrownNatural() {
        Block log = world.getBlockAt(0, 65, 0);
        tracker.add(log, BlockTag.PLACED); // a sapling the player planted
        listener.onStructureGrow(new org.bukkit.event.world.StructureGrowEvent(
                log.getLocation(), org.bukkit.TreeType.TREE, false, null, List.of(state(log, Material.OAK_LOG))));
        assertEquals(Set.of(BlockTag.GROWN_NATURAL), tracker.tags(log));
    }

    @Test
    void boneMealTreeGrowthGetsGrownBonemeal() {
        Block log = world.getBlockAt(0, 65, 0);
        listener.onStructureGrow(new org.bukkit.event.world.StructureGrowEvent(
                log.getLocation(), org.bukkit.TreeType.TREE, true, null, List.of(state(log, Material.OAK_LOG))));
        assertEquals(Set.of(BlockTag.GROWN_BONEMEAL), tracker.tags(log));
    }

    @Test
    void breakingAGrownBlockClearsItsTag() {
        Block log = world.getBlockAt(0, 65, 0);
        tracker.add(log, BlockTag.GROWN_NATURAL);
        listener.onBreak(new org.bukkit.event.block.BlockBreakEvent(log, MockBukkit.getMock().addPlayer()));
        assertTrue(tracker.tags(log).isEmpty());
    }

    @Test
    void placingOverAStaleGrownTagLeavesOnlyPlaced() {
        Block block = world.getBlockAt(0, 65, 0);
        tracker.add(block, BlockTag.GROWN_NATURAL); // a leaf that decayed, tag never cleared
        var player = MockBukkit.getMock().addPlayer();
        listener.onPlace(new org.bukkit.event.block.BlockPlaceEvent(
                block,
                block.getState(),
                world.getBlockAt(0, 64, 0),
                new org.bukkit.inventory.ItemStack(Material.DIRT),
                player,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND));
        assertEquals(Set.of(BlockTag.PLACED), tracker.tags(block));
    }

    @Test
    void fluidMadeBlocksAreTaggedFluid() {
        for (Material type : List.of(Material.COBBLESTONE, Material.STONE, Material.BASALT, Material.OBSIDIAN)) {
            Block block = world.getBlockAt(5, 64, 5);
            tracker.clear(block);
            block.setType(type);
            listener.onForm(new BlockFormEvent(block, block.getState()));
            assertTrue(tracker.has(block, BlockTag.FLUID), type.name());
        }
    }

    @Test
    void otherFormedBlocksAreNotTagged() {
        Block block = world.getBlockAt(6, 64, 5);
        block.setType(Material.SNOW);
        listener.onForm(new BlockFormEvent(block, block.getState()));
        assertTrue(tracker.tags(block).isEmpty());
    }

    @Test
    void pistonPushingARowKeepsEveryTag() {
        Block piston = piston(Material.PISTON, BlockFace.EAST);
        Block first = world.getBlockAt(1, 64, 0);
        Block second = world.getBlockAt(2, 64, 0);
        tracker.add(first, BlockTag.PLACED);
        tracker.add(second, BlockTag.PLACED);
        listener.onPistonExtend(new BlockPistonExtendEvent(piston, List.of(first, second), BlockFace.EAST));
        assertFalse(tracker.has(first, BlockTag.PLACED));
        assertTrue(tracker.has(second, BlockTag.PLACED));
        assertTrue(tracker.has(world.getBlockAt(3, 64, 0), BlockTag.PLACED));
    }
}
