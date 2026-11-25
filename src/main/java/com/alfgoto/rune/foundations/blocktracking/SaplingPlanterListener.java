package com.alfgoto.rune.foundations.blocktracking;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/** Feeds {@link SaplingPlanters}: a player places a sapling, it is remembered; a break or a dispenser forgets it. */
public final class SaplingPlanterListener implements Listener {

    private final SaplingPlanters planters;

    public SaplingPlanterListener(SaplingPlanters planters) {
        this.planters = planters;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (Tag.SAPLINGS.isTagged(event.getBlockPlaced().getType())) {
            planters.set(event.getBlockPlaced(), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (Tag.SAPLINGS.isTagged(event.getBlock().getType())) {
            planters.clear(event.getBlock());
        }
    }

    /** A dispenser has no planter: a sapling it places must not inherit an old one. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (Tag.SAPLINGS.isTagged(event.getItem().getType())
                && event.getBlock().getBlockData() instanceof Directional directional) {
            Block target = event.getBlock().getRelative(directional.getFacing());
            planters.clear(target);
        }
    }
}
