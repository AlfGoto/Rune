package com.alfgoto.rune.content.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.alfgoto.rune.gameplay.jobs.BlockActions;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import java.util.Optional;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class CoreBlockActionsTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static String job(Material material) {
        return CoreBlockActions.jobIdOf(material).orElse("none");
    }

    @Test
    void pickaxeBlocksBelongToMining() {
        assertEquals("mining", job(Material.STONE));
        assertEquals("mining", job(Material.DIAMOND_ORE));
        assertEquals("mining", job(Material.DEEPSLATE_GOLD_ORE));
    }

    @Test
    void theBlockDecidesTheJobNotTheTool() {
        // A log is Woodcutting whatever breaks it; stone is Mining whatever breaks it.
        assertEquals("woodcutting", job(Material.OAK_LOG));
        assertEquals("mining", job(Material.STONE));
    }

    @Test
    void axeBlocksBelongToWoodcutting() {
        assertEquals("woodcutting", job(Material.BIRCH_LOG));
        assertEquals("woodcutting", job(Material.CRIMSON_STEM));
        assertEquals("woodcutting", job(Material.MANGROVE_ROOTS));
    }

    @Test
    void leavesAndWartBlocksAreWoodcuttingAdditions() {
        assertEquals("woodcutting", job(Material.OAK_LEAVES));
        assertEquals("woodcutting", job(Material.NETHER_WART_BLOCK));
        assertEquals("woodcutting", job(Material.WARPED_WART_BLOCK));
        assertEquals("woodcutting", job(Material.SHROOMLIGHT));
    }

    @Test
    void axeCropsGoToFarmingAndBeesToRanching() {
        assertEquals("farming", job(Material.PUMPKIN));
        assertEquals("farming", job(Material.MELON));
        assertEquals("farming", job(Material.COCOA));
        assertEquals("farming", job(Material.BAMBOO));
        assertEquals("ranching", job(Material.BEE_NEST));
        assertEquals("ranching", job(Material.BEEHIVE));
    }

    @Test
    void shovelBlocksBelongToExcavation() {
        assertEquals("excavation", job(Material.DIRT));
        assertEquals("excavation", job(Material.SAND));
    }

    @Test
    void xpValuesFollowTheDocs() {
        assertEquals(1, CoreBlockActions.baseXp(Material.STONE).getAsDouble());
        assertEquals(0.25, CoreBlockActions.baseXp(Material.NETHERRACK).getAsDouble());
        assertEquals(1.5, CoreBlockActions.baseXp(Material.DEEPSLATE).getAsDouble());
        assertEquals(2, CoreBlockActions.baseXp(Material.END_STONE).getAsDouble());
        assertEquals(30, CoreBlockActions.baseXp(Material.DIAMOND_ORE).getAsDouble());
        assertEquals(37.5, CoreBlockActions.baseXp(Material.DEEPSLATE_DIAMOND_ORE).getAsDouble());
        assertEquals(6, CoreBlockActions.baseXp(Material.OBSIDIAN).getAsDouble());
        assertEquals(6, CoreBlockActions.baseXp(Material.WAXED_OXIDIZED_CUT_COPPER).getAsDouble());
        assertEquals(3, CoreBlockActions.baseXp(Material.OAK_LOG).getAsDouble());
        assertEquals(3, CoreBlockActions.baseXp(Material.STRIPPED_OAK_WOOD).getAsDouble());
        assertEquals(4, CoreBlockActions.baseXp(Material.CHERRY_LOG).getAsDouble());
        assertEquals(5, CoreBlockActions.baseXp(Material.PALE_OAK_LOG).getAsDouble());
        assertEquals(0.1, CoreBlockActions.baseXp(Material.OAK_LEAVES).getAsDouble());
        assertEquals(0.05, CoreBlockActions.baseXp(Material.VINE).getAsDouble());
        assertEquals(20, CoreBlockActions.baseXp(Material.CREAKING_HEART).getAsDouble());
    }

    @Test
    void blocksWithoutADocumentedValueGiveNothing() {
        assertFalse(CoreBlockActions.baseXp(Material.FURNACE).isPresent());
        assertFalse(CoreBlockActions.baseXp(Material.DIRT).isPresent());
    }

    @Test
    void onlyBlocksOfRegisteredJobsGetAnAction() {
        JobRegistry jobs = new JobRegistry();
        BlockActions actions = new BlockActions();
        CoreJobs.registerAll(jobs);
        CoreBlockActions.registerAll(actions, jobs);

        assertSame(CoreJobs.MINING, actions.get(Material.IRON_ORE).orElseThrow().job());
        assertSame(CoreJobs.WOODCUTTING, actions.get(Material.OAK_LEAVES).orElseThrow().job());
        assertEquals(Optional.empty(), actions.get(Material.PUMPKIN)); // Farming does not exist yet
        assertEquals(Optional.empty(), actions.get(Material.BEEHIVE)); // Ranching does not exist yet
        assertEquals(Optional.empty(), actions.get(Material.DIRT)); // Excavation does not exist yet
    }
}
