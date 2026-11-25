package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.StoneFamily;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.gameplay.veins.VeinScan;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Veinminer (docs/jobs/mining.md): mining an ore with a pickaxe that has the Rune also breaks the ores of the same
 * kind that touch it, up to the Rune's value in blocks (8 / 16 / 32 per Rank), the first block included. Ores that
 * Block Tracking remembers (placed, grown) are left alone. The extra blocks go through {@link MultiBreak}, so each
 * gives full Job XP and a Cache roll.
 */
final class VeinminerListener implements Listener {

    static final String EFFECT_ID = "veinminer";

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlockTracker tracker;
    private final MultiBreak multiBreak;
    private final Predicate<ItemStack> isBroken;

    VeinminerListener(
            ItemRegistry items,
            ToolLoadout loadout,
            BlockTracker tracker,
            MultiBreak multiBreak,
            Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.tracker = tracker;
        this.multiBreak = multiBreak;
        this.isBroken = isBroken;
    }

    @EventHandler
    public void onBlockBreak(RuneBlockBreakEvent event) {
        // Only the player's own break starts a vein: blocks broken by an effect never start another one.
        if (event.cause() != BreakCause.TOOL || items.of(event.item()).isEmpty()) {
            return;
        }
        Material ore = event.block().getType();
        if (!StoneFamily.isOre(ore) || !tracker.tags(event.block()).isEmpty()) {
            return;
        }
        int cap = (int) Stacking.total(EFFECT_ID, loadout.sources(event.item(), event.player()));
        if (cap <= 1) {
            return;
        }
        List<Block> vein = VeinScan.find(
                event.block(),
                cap - 1,
                block -> StoneFamily.sameOre(ore, block.getType()) && tracker.tags(block).isEmpty());
        multiBreak.breakAll(event.player(), vein, tool -> items.of(tool).isPresent() && !isBroken.test(tool));
    }
}
