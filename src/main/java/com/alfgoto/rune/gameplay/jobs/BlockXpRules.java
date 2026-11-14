package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import java.util.Set;
import org.bukkit.Material;

/**
 * Anti-exploit rules for block Job XP (docs/design-decisions.md, "Anti-exploit"):
 * placed = 0, fluid-made = 0 (obsidian excepted), grown with bone meal = ×multiplier.
 * Blocks broken by TNT or creepers never reach here: they are not player breaks.
 */
public final class BlockXpRules {

    private final double bonemealMultiplier;

    public BlockXpRules(double bonemealMultiplier) {
        this.bonemealMultiplier = bonemealMultiplier;
    }

    /** The factor to apply to a block's base Job XP, given its tags. 0 means no XP. */
    public double multiplier(Material material, Set<BlockTag> tags) {
        if (tags.contains(BlockTag.PLACED)) {
            return 0;
        }
        if (tags.contains(BlockTag.FLUID) && material != Material.OBSIDIAN) {
            return 0;
        }
        if (tags.contains(BlockTag.GROWN_BONEMEAL)) {
            return bonemealMultiplier;
        }
        return 1;
    }
}
