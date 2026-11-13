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
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/** Feeds a {@link BlockTracker} from Paper events: tag on place, clear on break, move with pistons. */
public final class BlockTrackingListener implements Listener {

    private static final Set<Material> FLUID_MADE =
            Set.of(Material.COBBLESTONE, Material.STONE, Material.BASALT, Material.OBSIDIAN);

    private final BlockTracker tracker;

    public BlockTrackingListener(BlockTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event instanceof BlockMultiPlaceEvent multi) {
            for (BlockState state : multi.getReplacedBlockStates()) {
                tracker.add(state.getBlock(), BlockTag.PLACED);
            }
        }
        tracker.add(event.getBlockPlaced(), BlockTag.PLACED);
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
