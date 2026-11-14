package com.alfgoto.rune.content.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.jobs.BlockActions;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
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

    /** The shipped config.yml, read with a logger that records errors (a wrong block name is an error). */
    private static RuneConfig shippedConfig(List<String> errors) throws Exception {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord r) {
                if (r.getLevel() == Level.SEVERE) {
                    errors.add(r.getMessage());
                }
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        });
        try (var in = CoreBlockActionsTest.class.getResourceAsStream("/config.yml")) {
            assertNotNull(in);
            return RuneConfig.load(YamlConfiguration.loadConfiguration(new InputStreamReader(in)), logger);
        }
    }

    private static BlockActions shippedActions() throws Exception {
        JobRegistry jobs = new JobRegistry();
        BlockActions actions = new BlockActions();
        CoreJobs.registerAll(jobs);
        CoreBlockActions.registerAll(actions, jobs, shippedConfig(new ArrayList<>()).blockXp());
        return actions;
    }

    private static double xp(BlockActions actions, Material material) {
        return actions.get(material).orElseThrow().xp();
    }

    @Test
    void everyShippedBlockNameIsARealBlock() throws Exception {
        List<String> errors = new ArrayList<>();
        RuneConfig config = shippedConfig(errors);
        assertTrue(errors.isEmpty(), errors.toString());
        assertFalse(config.blockXp().isEmpty());
    }

    @Test
    void xpValuesFollowTheDocs() throws Exception {
        BlockActions actions = shippedActions();
        assertEquals(1, xp(actions, Material.STONE));
        assertEquals(0.25, xp(actions, Material.NETHERRACK));
        assertEquals(1.5, xp(actions, Material.DEEPSLATE));
        assertEquals(2, xp(actions, Material.END_STONE));
        assertEquals(30, xp(actions, Material.DIAMOND_ORE));
        assertEquals(37.5, xp(actions, Material.DEEPSLATE_DIAMOND_ORE));
        assertEquals(6, xp(actions, Material.OBSIDIAN));
        assertEquals(6, xp(actions, Material.WAXED_OXIDIZED_CUT_COPPER));
        assertEquals(3, xp(actions, Material.OAK_LOG));
        assertEquals(3, xp(actions, Material.STRIPPED_OAK_WOOD));
        assertEquals(4, xp(actions, Material.CHERRY_LOG));
        assertEquals(5, xp(actions, Material.PALE_OAK_LOG));
        assertEquals(0.1, xp(actions, Material.OAK_LEAVES));
        assertEquals(0.05, xp(actions, Material.VINE));
        assertEquals(20, xp(actions, Material.CREAKING_HEART));
    }

    @Test
    void blocksWithoutAConfiguredValueGiveNothing() throws Exception {
        BlockActions actions = shippedActions();
        assertFalse(actions.get(Material.FURNACE).isPresent());
        assertFalse(actions.get(Material.DIRT).isPresent());
    }

    @Test
    void onlyBlocksOfRegisteredJobsGetAnAction() {
        JobRegistry jobs = new JobRegistry();
        BlockActions actions = new BlockActions();
        CoreJobs.registerAll(jobs);
        CoreBlockActions.registerAll(
                actions,
                jobs,
                Map.of(
                        Material.IRON_ORE, 8.0,
                        Material.OAK_LEAVES, 0.1,
                        Material.PUMPKIN, 1.0,
                        Material.BEEHIVE, 1.0,
                        Material.DIRT, 1.0));

        assertSame(CoreJobs.MINING, actions.get(Material.IRON_ORE).orElseThrow().job());
        assertSame(CoreJobs.WOODCUTTING, actions.get(Material.OAK_LEAVES).orElseThrow().job());
        assertEquals(Optional.empty(), actions.get(Material.PUMPKIN)); // Farming does not exist yet
        assertEquals(Optional.empty(), actions.get(Material.BEEHIVE)); // Ranching does not exist yet
        assertEquals(Optional.empty(), actions.get(Material.DIRT)); // Excavation does not exist yet
    }
}
