package com.alfgoto.rune.gameplay.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import java.util.Set;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class BlockXpRulesTest {

    private final BlockXpRules rules = new BlockXpRules(0.25);

    @Test
    void untaggedBlockGivesFullXp() {
        assertEquals(1, rules.multiplier(Material.DIAMOND_ORE, Set.of()));
    }

    @Test
    void naturalGrowthGivesFullXp() {
        assertEquals(1, rules.multiplier(Material.OAK_LOG, Set.of(BlockTag.GROWN_NATURAL)));
    }

    @Test
    void placedBlockGivesNothing() {
        assertEquals(0, rules.multiplier(Material.DIAMOND_ORE, Set.of(BlockTag.PLACED)));
    }

    @Test
    void placedObsidianGivesNothing() {
        assertEquals(0, rules.multiplier(Material.OBSIDIAN, Set.of(BlockTag.PLACED)));
    }

    @Test
    void fluidMadeBlockGivesNothing() {
        assertEquals(0, rules.multiplier(Material.COBBLESTONE, Set.of(BlockTag.FLUID)));
        assertEquals(0, rules.multiplier(Material.BASALT, Set.of(BlockTag.FLUID)));
    }

    @Test
    void fluidMadeObsidianKeepsItsXp() {
        assertEquals(1, rules.multiplier(Material.OBSIDIAN, Set.of(BlockTag.FLUID)));
    }

    @Test
    void boneMealGrownBlockGivesTheConfiguredFraction() {
        assertEquals(0.25, rules.multiplier(Material.OAK_LOG, Set.of(BlockTag.GROWN_BONEMEAL)));
        assertEquals(0.5, new BlockXpRules(0.5).multiplier(Material.OAK_LOG, Set.of(BlockTag.GROWN_BONEMEAL)));
    }
}
