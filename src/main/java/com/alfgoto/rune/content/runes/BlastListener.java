package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.blast.BlastArea;
import com.alfgoto.rune.gameplay.blast.BlastCharges;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Blast Mine and Great Blast (docs/jobs/mining.md): after the click opened the Rune's window, every pickaxe block the
 * player mines with that tool during the window also breaks the blocks around it: a 3x3 square flat to the mined face (Blast Mine) or
 * a 3x3x3 cube (Great Blast). Only pickaxe blocks that can be broken are taken. The extra blocks go through
 * {@link MultiBreak}, so each gives full Job XP and a Cache roll. Great Blast wins when both are armed.
 */
final class BlastListener implements Listener {

    static final String BLAST_MINE = "blast_mine";
    static final String GREAT_BLAST = "great_blast";

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlastCharges charges;
    private final MultiBreak multiBreak;
    private final Predicate<ItemStack> isBroken;

    BlastListener(
            ItemRegistry items,
            ToolLoadout loadout,
            BlastCharges charges,
            MultiBreak multiBreak,
            Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.charges = charges;
        this.multiBreak = multiBreak;
        this.isBroken = isBroken;
    }

    @EventHandler
    public void onBlockBreak(RuneBlockBreakEvent event) {
        // Only the player's own break sets a blast off: blocks broken by an effect never start another one.
        if (event.cause() != BreakCause.TOOL
                || items.of(event.item()).isEmpty()
                || !Tag.MINEABLE_PICKAXE.isTagged(event.block().getType())) {
            return;
        }
        var id = event.player().getUniqueId();
        List<Block> blocks;
        if (installed(event, GREAT_BLAST) && charges.isArmed(id, GREAT_BLAST)) {
            blocks = BlastArea.cube(event.block(), BlastListener::breakable);
        } else if (installed(event, BLAST_MINE) && charges.isArmed(id, BLAST_MINE)) {
            blocks = BlastArea.square(
                    event.block(), BlastArea.faceAxis(event.player(), event.block()), BlastListener::breakable);
        } else {
            return;
        }
        multiBreak.breakAll(event.player(), blocks, tool -> items.of(tool).isPresent() && !isBroken.test(tool));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        charges.clear(event.getPlayer().getUniqueId());
    }

    private boolean installed(RuneBlockBreakEvent event, String effectId) {
        return Stacking.total(effectId, loadout.sources(event.item(), event.player())) > 0;
    }

    /** A pickaxe block that can be broken (not air, fluid-only or bedrock-like). */
    static boolean breakable(Block block) {
        return !block.getType().isAir()
                && Tag.MINEABLE_PICKAXE.isTagged(block.getType())
                && block.getType().getHardness() >= 0;
    }
}
