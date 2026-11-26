package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.trees.TreeScan;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Treefeller (docs/jobs/woodcutting.md): cutting a natural log with an axe that has the Rune also breaks the tree,
 * up to the Rune's value in blocks (16 / 32 / 64 per Rank), the first block included. The blocks come from
 * {@link TreeScan#scanTree}: logs of the same wood type at or above the broken one, then the canopy touching them.
 * The extra blocks go through {@link MultiBreak}, so each gives full Job XP and a Cache roll.
 */
final class TreefellerListener implements Listener {

    static final String EFFECT_ID = "treefeller";

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlockTracker tracker;
    private final TreeScan scan;
    private final MultiBreak multiBreak;
    private final Predicate<ItemStack> isBroken;

    TreefellerListener(
            ItemRegistry items,
            ToolLoadout loadout,
            BlockTracker tracker,
            TreeScan scan,
            MultiBreak multiBreak,
            Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.tracker = tracker;
        this.scan = scan;
        this.multiBreak = multiBreak;
        this.isBroken = isBroken;
    }

    @EventHandler
    public void onBlockBreak(RuneBlockBreakEvent event) {
        // Only the player's own break starts a felling: blocks broken by an effect never start another one.
        if (event.cause() != BreakCause.TOOL || items.of(event.item()).isEmpty()) {
            return;
        }
        Block origin = event.block();
        if (!Tag.LOGS.isTagged(origin.getType()) || tracker.has(origin, BlockTag.PLACED)) {
            return;
        }
        int cap = (int) Stacking.total(EFFECT_ID, loadout.sources(event.item(), event.player()));
        if (cap <= 1) {
            return;
        }
        List<Block> tree = scan.scanTree(origin, cap - 1);
        multiBreak.breakAll(event.player(), tree, tool -> items.of(tool).isPresent() && !isBroken.test(tool));
    }
}
