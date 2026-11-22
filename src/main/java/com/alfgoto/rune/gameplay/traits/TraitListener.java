package com.alfgoto.rune.gameplay.traits;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolMastery;
import com.alfgoto.rune.gameplay.jobs.BlockXpRules;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Feeds the Copper and Momentum Traits from the player's breaks with the tool in hand.
 *
 * <p>Copper counts a block like Mastery does: placed and fluid-made blocks count 0, bone-meal-grown blocks count
 * less, so a player cannot age the tool by placing and breaking blocks. It reads the tags before Block
 * Tracking clears them (see {@code TraitsModule}). Momentum counts every block for a pickaxe, and logs only for
 * an axe ("per log in a row").
 */
final class TraitListener implements Listener {

    private final ItemRegistry items;
    private final CopperOxidation copper;
    private final Momentum momentum;
    private final BlockXpRules rules;
    private final BlockTracker tracker;

    TraitListener(
            ItemRegistry items,
            CopperOxidation copper,
            Momentum momentum,
            BlockXpRules rules,
            BlockTracker tracker) {
        this.items = items;
        this.copper = copper;
        this.momentum = momentum;
        this.rules = rules;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(RuneBlockBreakEvent event) {
        ItemStack tool = event.item();
        RuneItem item = items.of(tool).orElse(null);
        if (item == null || item.toolSpec().isEmpty()) {
            return;
        }
        Material type = event.block().getType();
        if (CopperOxidation.hasScale(item, CopperOxidation.SCALE_ID)) {
            double weight = rules.multiplier(type, tracker.tags(event.block()));
            if (weight > 0) {
                copper.add(tool, weight);
            }
        }
        if (CopperOxidation.hasScale(item, Momentum.SCALE_ID) && countsForMomentum(item, type)) {
            momentum.hit(event.player().getUniqueId());
        }
    }

    private boolean countsForMomentum(RuneItem item, Material block) {
        return items.toolStats(item).map(stats -> stats.kind() != ToolKind.AXE || ToolMastery.isLog(block)).orElse(false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        momentum.forget(event.getPlayer().getUniqueId());
    }
}
