package com.alfgoto.rune.foundations.blocktracking;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * Feeds a {@link BlockTracker} from Paper events: tag on place, tag grown blocks, clear on break, move with
 * pistons.
 */
public final class BlockTrackingListener implements Listener {

    private static final Set<Material> FLUID_MADE =
            Set.of(Material.COBBLESTONE, Material.STONE, Material.BASALT, Material.OBSIDIAN);

    /** Plants that spread or grow one block at a time (other growths come as a structure). */
    private static final Set<Material> SPREADING = Set.of(
            Material.VINE,
            Material.GLOW_LICHEN,
            Material.BIG_DRIPLEAF,
            Material.BIG_DRIPLEAF_STEM,
            Material.CHORUS_PLANT,
            Material.CHORUS_FLOWER);

    private final BlockTracker tracker;

    public BlockTrackingListener(BlockTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event instanceof BlockMultiPlaceEvent multi) {
            for (BlockState state : multi.getReplacedBlockStates()) {
                markPlaced(state.getBlock());
            }
        }
        markPlaced(event.getBlockPlaced());
    }

    /** A placed block is not grown: drop any stale tag left at this spot first. */
    private void markPlaced(Block block) {
        tracker.clear(block);
        tracker.add(block, BlockTag.PLACED);
    }

    /** Trees, giant mushrooms, huge fungi, mangrove propagules. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        BlockTag tag = event.isFromBonemeal() ? BlockTag.GROWN_BONEMEAL : BlockTag.GROWN_NATURAL;
        for (BlockState state : event.getBlocks()) {
            if (!state.getType().isAir()) {
                markGrown(state.getBlock(), tag);
            }
        }
    }

    /** Bone meal on glow lichen and big dripleaf. Other bone meal results are ignored. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event) {
        for (BlockState state : event.getBlocks()) {
            if (SPREADING.contains(state.getType())) {
                markGrown(state.getBlock(), BlockTag.GROWN_BONEMEAL);
            }
        }
    }

    /** Vines and glow lichen spreading over time. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        if (SPREADING.contains(event.getNewState().getType())) {
            markGrown(event.getBlock(), BlockTag.GROWN_NATURAL);
        }
    }

    /** Chorus plants and flowers growing over time. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        if (SPREADING.contains(event.getNewState().getType())) {
            markGrown(event.getBlock(), BlockTag.GROWN_NATURAL);
        }
    }

    /** A growth replaces what was there, so old tags (a planted sapling, say) must go. */
    private void markGrown(Block block, BlockTag tag) {
        tracker.clear(block);
        tracker.add(block, tag);
    }

    /** Fluids turning into a block: cobblestone / stone / basalt generators, and obsidian. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onForm(BlockFormEvent event) {
        if (FLUID_MADE.contains(event.getNewState().getType())) {
            tracker.add(event.getBlock(), BlockTag.FLUID);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        tracker.clear(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        move(event.getBlocks(), facing(event.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        move(event.getBlocks(), facing(event.getBlock()).getOppositeFace());
    }

    private static BlockFace facing(Block piston) {
        return piston.getBlockData() instanceof Directional directional ? directional.getFacing() : BlockFace.UP;
    }

    /** Two phases, so blocks pushed into each other's old spot keep the right tags. */
    private void move(List<Block> blocks, BlockFace direction) {
        List<Set<BlockTag>> before = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            before.add(tracker.tags(block));
            tracker.clear(block);
        }
        for (int i = 0; i < blocks.size(); i++) {
            Block target = blocks.get(i).getRelative(direction);
            for (BlockTag tag : before.get(i)) {
                tracker.add(target, tag);
            }
        }
    }
}
