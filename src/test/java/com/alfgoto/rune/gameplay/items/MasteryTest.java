package com.alfgoto.rune.gameplay.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingListener;
import com.alfgoto.rune.foundations.blocktracking.PdcBlockTracker;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.gameplay.jobs.BlockXpRules;
import com.alfgoto.rune.text.Rarity;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class MasteryTest {

    private ServerMock server;
    private World world;
    private PlayerMock player;
    private BlockTracker tracker;
    private ItemRegistry registry;
    private ToolMastery mastery;
    private ItemStack pickaxe;
    private ItemStack axe;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("w");
        player = server.addPlayer();
        Plugin plugin = MockBukkit.createMockPlugin();
        ToolConfig config = ToolConfig.defaults();
        registry = new ItemRegistry(plugin, config);
        mastery = new ToolMastery(plugin, registry, config);
        // Common tools have Capacity 3: +75% = 2.25, so at most +2. Rare: 10 → +7.
        registry.register(RuneItem.tool("m_pick", Rarity.COMMON, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        registry.register(RuneItem.tool("m_axe", Rarity.RARE, ToolKind.AXE, "test_rune", ToolSpec.DEFAULT));
        registry.register(RuneItem.tool("m_shovel", Rarity.RARE, ToolKind.SHOVEL, "test_rune", ToolSpec.DEFAULT));
        registry.register(new RuneItem("m_plain", Rarity.COMMON, Material.PAPER, "test_rune"));
        pickaxe = registry.create(registry.get("m_pick"), 1);
        axe = registry.create(registry.get("m_axe"), 1);

        tracker = new PdcBlockTracker("rune");
        Plugin listener = MockBukkit.createMockPlugin();
        server.getPluginManager().registerEvents(new Triggers(server), listener);
        server.getPluginManager().registerEvents(new BlockTrackingListener(tracker), listener);
        server.getPluginManager()
                .registerEvents(new MasteryListener(registry, mastery, new BlockXpRules(0.25), tracker), listener);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void breakWith(ItemStack tool, Material type, BlockTag... tags) {
        player.getInventory().setItemInMainHand(tool);
        Block block = world.getBlockAt(1, 64, 1);
        block.setType(type);
        for (BlockTag tag : tags) {
            tracker.add(block, tag);
        }
        server.getPluginManager().callEvent(new BlockBreakEvent(block, player));
    }

    private ItemStack held() {
        return player.getInventory().getItemInMainHand();
    }

    // --- the pure counter ---

    @Test
    void bonusIsOnePerStepAndRoundsDown() {
        assertEquals(0, ToolMastery.bonus(0, 500, 7));
        assertEquals(0, ToolMastery.bonus(499, 500, 7));
        assertEquals(1, ToolMastery.bonus(500, 500, 7));
        assertEquals(1, ToolMastery.bonus(999.9, 500, 7));
        assertEquals(3, ToolMastery.bonus(1500, 500, 7));
    }

    @Test
    void bonusStopsAtTheCap() {
        assertEquals(7, ToolMastery.bonus(3500, 500, 7));
        assertEquals(7, ToolMastery.bonus(1_000_000, 500, 7));
    }

    @Test
    void capIsSeventyFivePercentOfBaseCapacityRoundedDown() {
        assertEquals(2, mastery.maxBonus(3));
        assertEquals(4, mastery.maxBonus(6));
        assertEquals(7, mastery.maxBonus(10));
        assertEquals(10, mastery.maxBonus(14));
        assertEquals(13, mastery.maxBonus(18));
    }

    @Test
    void pickaxeCountsOresAndAxeCountsLogs() {
        assertTrue(ToolMastery.counts(ToolKind.PICKAXE, Material.DIAMOND_ORE));
        assertTrue(ToolMastery.counts(ToolKind.PICKAXE, Material.DEEPSLATE_IRON_ORE));
        assertTrue(ToolMastery.counts(ToolKind.PICKAXE, Material.NETHER_QUARTZ_ORE));
        assertTrue(ToolMastery.counts(ToolKind.PICKAXE, Material.ANCIENT_DEBRIS));
        assertFalse(ToolMastery.counts(ToolKind.PICKAXE, Material.STONE));
        assertFalse(ToolMastery.counts(ToolKind.PICKAXE, Material.OAK_LOG));
        assertTrue(ToolMastery.counts(ToolKind.AXE, Material.OAK_LOG));
        assertTrue(ToolMastery.counts(ToolKind.AXE, Material.CRIMSON_STEM));
        assertTrue(ToolMastery.counts(ToolKind.AXE, Material.MUSHROOM_STEM));
        assertTrue(ToolMastery.counts(ToolKind.AXE, Material.CHORUS_PLANT));
        assertFalse(ToolMastery.counts(ToolKind.AXE, Material.OAK_LEAVES));
        assertFalse(ToolMastery.counts(ToolKind.AXE, Material.DIAMOND_ORE));
        assertFalse(ToolMastery.counts(ToolKind.SHOVEL, Material.DIRT));
    }

    // --- on a stack ---

    @Test
    void aNewToolHasNoMasteryYet() {
        MasteryStatus status = mastery.status(pickaxe).orElseThrow();
        assertEquals(0, status.bonus());
        assertEquals(2, status.maxBonus());
        assertEquals(500, status.perCapacity());
        assertEquals(3, status.capacity());
    }

    @Test
    void shovelsAndPlainItemsHaveNoMastery() {
        assertTrue(mastery.status(registry.create(registry.get("m_shovel"), 1)).isEmpty());
        assertTrue(mastery.status(registry.create(registry.get("m_plain"), 1)).isEmpty());
        assertTrue(mastery.status(new ItemStack(Material.DIAMOND_PICKAXE)).isEmpty());
    }

    @Test
    void addingActionsRaisesCapacityEveryStep() {
        assertTrue(mastery.add(pickaxe, 499));
        assertEquals(3, mastery.status(pickaxe).orElseThrow().capacity());
        assertTrue(mastery.add(pickaxe, 1));
        MasteryStatus status = mastery.status(pickaxe).orElseThrow();
        assertEquals(4, status.capacity());
        assertEquals(0, status.towardsNext());
        mastery.add(pickaxe, 120);
        assertEquals(120, mastery.status(pickaxe).orElseThrow().towardsNext());
    }

    @Test
    void masteryStopsAtTheCap() {
        mastery.add(pickaxe, 100_000);
        MasteryStatus status = mastery.status(pickaxe).orElseThrow();
        assertEquals(2, status.bonus());
        assertTrue(status.maxed());
        assertEquals(5, status.capacity());
        assertFalse(mastery.add(pickaxe, 1)); // nothing more to earn
    }

    @Test
    void axesNeedAThousandLogs() {
        mastery.add(axe, 999);
        assertEquals(0, mastery.status(axe).orElseThrow().bonus());
        mastery.add(axe, 1);
        assertEquals(1, mastery.status(axe).orElseThrow().bonus());
    }

    @Test
    void progressSurvivesAsItemData() {
        mastery.add(pickaxe, 42);
        assertEquals(42, mastery.progress(pickaxe), 1e-9);
        assertEquals(42, mastery.progress(pickaxe.clone()), 1e-9);
    }

    @Test
    void addingToAnItemWithoutMasteryDoesNothing() {
        ItemStack plain = registry.create(registry.get("m_plain"), 1);
        assertFalse(mastery.add(plain, 10));
        assertFalse(mastery.add(pickaxe, 0));
        assertFalse(mastery.add(pickaxe, -5));
        assertEquals(0, mastery.progress(pickaxe), 1e-9);
    }

    // --- counting while playing ---

    @Test
    void breakingAnOreWithAPickaxeCounts() {
        breakWith(pickaxe, Material.DIAMOND_ORE);
        assertEquals(1, mastery.progress(held()), 1e-9);
    }

    @Test
    void breakingStoneOrLogsWithAPickaxeDoesNotCount() {
        breakWith(pickaxe, Material.STONE);
        breakWith(held(), Material.OAK_LOG);
        assertEquals(0, mastery.progress(held()), 1e-9);
    }

    @Test
    void breakingALogWithAnAxeCountsButLeavesDoNot() {
        breakWith(axe, Material.OAK_LOG);
        breakWith(held(), Material.OAK_LEAVES);
        assertEquals(1, mastery.progress(held()), 1e-9);
    }

    @Test
    void boneMealLogsCountAQuarter() {
        breakWith(axe, Material.OAK_LOG, BlockTag.GROWN_BONEMEAL);
        assertEquals(0.25, mastery.progress(held()), 1e-9);
    }

    @Test
    void placedAndFluidMadeBlocksDoNotCount() {
        breakWith(pickaxe, Material.DIAMOND_ORE, BlockTag.PLACED);
        assertEquals(0, mastery.progress(held()), 1e-9);
        breakWith(held(), Material.DIAMOND_ORE, BlockTag.FLUID);
        assertEquals(0, mastery.progress(held()), 1e-9);
    }

    @Test
    void breakingWithAVanillaToolOrBareHandsCountsNothing() {
        breakWith(new ItemStack(Material.WOODEN_PICKAXE), Material.DIAMOND_ORE);
        breakWith(new ItemStack(Material.AIR), Material.DIAMOND_ORE);
        assertEquals(0, mastery.progress(pickaxe), 1e-9);
    }
}
