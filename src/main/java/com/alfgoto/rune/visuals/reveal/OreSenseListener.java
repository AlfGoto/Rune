package com.alfgoto.rune.visuals.reveal;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.StoneFamily;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Ore Sense (docs/jobs/mining.md): each natural (not placed) stone-family block the player breaks has a chance to outline the ores
 * within a radius, with Block Reveal. The effect id {@code ore_sense} is shared, so a Trait (Bone Pickaxe) stacks
 * with the Rune. It only shows things: nothing in the world changes.
 */
final class OreSenseListener implements Listener {

    static final String EFFECT_ID = "ore_sense";
    static final Color COLOR = Color.AQUA;

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlockReveal reveal;
    private final BlockTracker tracker;
    private final DoubleSupplier random;
    private final int radius;
    private final int ticks;

    OreSenseListener(
            ItemRegistry items,
            ToolLoadout loadout,
            BlockReveal reveal,
            BlockTracker tracker,
            DoubleSupplier random,
            int radius,
            int seconds) {
        this.items = items;
        this.loadout = loadout;
        this.reveal = reveal;
        this.tracker = tracker;
        this.random = random;
        this.radius = radius;
        this.ticks = seconds * 20;
    }

    @EventHandler
    public void onBlockBreak(RuneBlockBreakEvent event) {
        if (!StoneFamily.isStone(event.block().getType())
                || tracker.has(event.block(), BlockTag.PLACED)
                || items.of(event.item()).isEmpty()) {
            return;
        }
        // One roll is enough: a second outline of the same ores would add nothing.
        if (Stacking.rolls(Stacking.total(EFFECT_ID, loadout.sources(event.item())), random) < 1) {
            return;
        }
        List<Block> ores = new ArrayList<>();
        Block center = event.block();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (StoneFamily.isOre(block.getType())) {
                        ores.add(block);
                    }
                }
            }
        }
        reveal.reveal(event.player(), ores, ticks, COLOR);
    }
}
