package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.jobs.BlockXpRules;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Counts a valuable Job Action for the Rune Tool in the player's hand: an ore for a pickaxe, a log
 * for an axe. The same rules as Job XP apply (placed and fluid-made blocks count 0, bone-meal-grown
 * blocks count ×multiplier), so it reads the tags before Block Tracking clears them: see
 * {@code MasteryModule}.
 */
public final class MasteryListener implements Listener {

    private final ItemRegistry items;
    private final ToolMastery mastery;
    private final BlockXpRules rules;
    private final BlockTracker tracker;

    public MasteryListener(ItemRegistry items, ToolMastery mastery, BlockXpRules rules, BlockTracker tracker) {
        this.items = items;
        this.mastery = mastery;
        this.rules = rules;
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(RuneBlockBreakEvent event) {
        ItemStack tool = event.item();
        Optional<ToolStats> stats = items.of(tool).flatMap(items::toolStats);
        if (stats.isEmpty()) {
            return;
        }
        Material type = event.block().getType();
        if (!ToolMastery.counts(stats.get().kind(), type)) {
            return;
        }
        double weight = rules.multiplier(type, tracker.tags(event.block()));
        if (weight > 0) {
            mastery.add(tool, weight);
        }
    }
}
