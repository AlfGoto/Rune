package com.alfgoto.rune.content.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.places.Place;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;
import com.alfgoto.rune.gameplay.places.WorldProbe;
import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.SaplingPlanters;
import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.quests.QuestAction;
import com.alfgoto.rune.gameplay.quests.QuestActionEvent;
import com.alfgoto.rune.gameplay.quests.QuestType;
import com.alfgoto.rune.gameplay.quests.QuestTypeRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

class CoreQuestsModuleTest {

    private ServerMock server;
    private RunePlugin plugin;
    private WorldMock world;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("quests");
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Block block(Material type, int y) {
        Block block = world.getBlockAt(0, y, 0);
        block.setType(type);
        return block;
    }

    @Test
    void collectCountsIronOreOnly() {
        assertTrue(CoreQuestsModule.isCollectBlock(block(Material.IRON_ORE, 60)));
        assertTrue(CoreQuestsModule.isCollectBlock(block(Material.DEEPSLATE_IRON_ORE, 10)));
        assertFalse(CoreQuestsModule.isCollectBlock(block(Material.COAL_ORE, 60)));
        assertFalse(CoreQuestsModule.isCollectBlock(block(Material.STONE, 60)));
    }

    @Test
    void depthCountsOresBelowMinus32Only() {
        assertTrue(CoreQuestsModule.isDeepOre(block(Material.DIAMOND_ORE, -33)));
        assertTrue(CoreQuestsModule.isDeepOre(block(Material.DEEPSLATE_GOLD_ORE, -50)));
        assertTrue(CoreQuestsModule.isDeepOre(block(Material.ANCIENT_DEBRIS, -40)));
        assertFalse(CoreQuestsModule.isDeepOre(block(Material.DIAMOND_ORE, -32)), "-32 is not below -32");
        assertFalse(CoreQuestsModule.isDeepOre(block(Material.DIAMOND_ORE, 20)));
        assertFalse(CoreQuestsModule.isDeepOre(block(Material.DEEPSLATE, -50)), "not an ore");
    }

    @Test
    void toolCountsTheCoalPickaxeOnly() {
        ItemRegistry items = new ItemRegistry(plugin);
        RuneItem coal = items.register(
                RuneItem.tool("coal_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "coal_pickaxe", ToolSpec.DEFAULT));
        RuneItem flint = items.register(
                RuneItem.tool("flint_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "flint_pickaxe", ToolSpec.DEFAULT));
        assertTrue(CoreQuestsModule.isHeldItem(items, items.create(coal, 1)));
        assertFalse(CoreQuestsModule.isHeldItem(items, items.create(flint, 1)));
        assertFalse(CoreQuestsModule.isHeldItem(items, new ItemStack(Material.WOODEN_PICKAXE)));
        assertFalse(CoreQuestsModule.isHeldItem(items, null));
    }

    @Test
    void theFourTypesAreRegisteredForMining() {
        var types = plugin.context().get(QuestTypeRegistry.class);
        for (String id : new String[] {"collect", "depth", "tool", "place"}) {
            QuestType type = types.registry().get(id);
            assertEquals(CoreJobs.MINING, type.job());
        }
        assertEquals(20, types.registry().get("collect").baseSize());
        assertEquals(10, types.registry().get("depth").baseSize());
        assertEquals(100, types.registry().get("tool").baseSize());
        assertEquals(50, types.registry().get("place").baseSize());
    }

    @Test
    void placeDoesNotCountOutsideAMineshaft() {
        var types = plugin.context().get(QuestTypeRegistry.class);
        QuestType place = types.registry().get("place");
        Block stone = block(Material.STONE, 60);
        assertFalse(place.objective().counts(new JobActionEvent(player, CoreJobs.MINING, stone, null)));
    }

    /** Probe that reports a structure only at one block (x, y, z) of the test world. */
    private PlaceDetector detector(String structure, int x, int y, int z) {
        PlaceRegistry registry = new PlaceRegistry();
        registry.register(Place.structure("mineshaft", "minecraft:mineshaft", "minecraft:mineshaft_mesa"));
        WorldProbe probe = new WorldProbe() {
            @Override
            public String biomeAt(Location l) {
                return "minecraft:plains";
            }

            @Override
            public Set<String> structuresAt(Location l) {
                boolean here = l.getBlockX() == x && l.getBlockY() == y && l.getBlockZ() == z;
                return here ? Set.of(structure) : Set.of();
            }
        };
        return new PlaceDetector(registry, probe);
    }

    @Test
    void placeCountsBothMineshaftTypesByBrokenBlock() {
        Block stone = block(Material.STONE, 40);
        JobActionEvent event = new JobActionEvent(player, CoreJobs.MINING, stone, null);
        assertTrue(CoreQuestsModule.isInPlace(detector("minecraft:mineshaft", 0, 40, 0), event));
        assertTrue(CoreQuestsModule.isInPlace(detector("minecraft:mineshaft_mesa", 0, 40, 0), event));
        assertFalse(CoreQuestsModule.isInPlace(detector("minecraft:fortress", 0, 40, 0), event));
    }

    @Test
    void placeCountsWhenThePlayerStandsInTheMineshaftTunnel() {
        // The mined wall block is outside every piece; the player's own block is inside.
        player.teleport(new Location(world, 5, 40, 5));
        Block wall = block(Material.STONE, 40);
        JobActionEvent event = new JobActionEvent(player, CoreJobs.MINING, wall, null);
        assertTrue(CoreQuestsModule.isInPlace(detector("minecraft:mineshaft", 5, 40, 5), event));
        assertFalse(CoreQuestsModule.isInPlace(detector("minecraft:mineshaft", 9, 40, 9), event));
    }

    @Test
    void woodcuttingTypesAreRegistered() {
        var types = plugin.context().get(QuestTypeRegistry.class).registry();
        assertEquals(64, types.get("wood_collect").baseSize());
        assertEquals(40, types.get("wood_place").baseSize());
        assertEquals(5, types.get("wood_variety").baseSize());
        assertEquals(20, types.get("wood_tool").baseSize());
        assertEquals(25, types.get("reforestation").baseSize());
        assertEquals(QuestAction.TREE_FELLED, types.get("wood_tool").action());
        assertEquals(QuestAction.TREE_GROWN, types.get("reforestation").action());
        assertEquals(12, types.get("wood_variety").maxSize());
    }

    @Test
    void woodCollectCountsSpruceLogAndWoodOnly() {
        assertTrue(CoreQuestsModule.isSpruceLog(block(Material.SPRUCE_LOG, 60)));
        assertTrue(CoreQuestsModule.isSpruceLog(block(Material.SPRUCE_WOOD, 60)));
        assertTrue(CoreQuestsModule.isSpruceLog(block(Material.STRIPPED_SPRUCE_LOG, 60)));
        assertTrue(CoreQuestsModule.isSpruceLog(block(Material.STRIPPED_SPRUCE_WOOD, 60)));
        assertFalse(CoreQuestsModule.isSpruceLog(block(Material.OAK_LOG, 60)));
        assertFalse(CoreQuestsModule.isSpruceLog(block(Material.SPRUCE_LEAVES, 60)));
    }

    @Test
    void woodTypesCoverEveryFamilyAndNothingElse() {
        assertEquals("oak", WoodTypes.of(Material.OAK_LOG));
        assertEquals("dark_oak", WoodTypes.of(Material.DARK_OAK_WOOD));
        assertEquals("pale_oak", WoodTypes.of(Material.PALE_OAK_LOG));
        assertEquals("crimson", WoodTypes.of(Material.CRIMSON_STEM));
        assertEquals("warped", WoodTypes.of(Material.STRIPPED_WARPED_HYPHAE));
        assertEquals("poplar", WoodTypes.of(Material.POPLAR_LOG));
        assertNull(WoodTypes.of(Material.OAK_PLANKS));
        assertNull(WoodTypes.of(Material.BAMBOO_BLOCK));
        assertNull(WoodTypes.of(Material.STONE));
    }

    @Test
    void woodPlaceNeedsALogInTheDarkForest() {
        QuestType place = plugin.context().get(QuestTypeRegistry.class).registry().get("wood_place");
        Block stone = block(Material.STONE, 60);
        assertFalse(place.objective().counts(new JobActionEvent(player, CoreJobs.WOODCUTTING, stone, null)));
        Block log = block(Material.OAK_LOG, 60);
        // The test world has no Dark Forest biome.
        assertFalse(place.objective().counts(new JobActionEvent(player, CoreJobs.WOODCUTTING, log, null)));
    }

    @Test
    void woodToolCountsTheJungleAxeOnly() {
        ItemRegistry items = new ItemRegistry(plugin);
        RuneItem jungle = items.register(
                RuneItem.tool("jungle_axe", Rarity.RARE, ToolKind.AXE, "jungle_axe", ToolSpec.DEFAULT));
        assertTrue(CoreQuestsModule.isHeldItem(items, items.create(jungle, 1), CoreQuestsModule.WOOD_TOOL_ID));
        assertFalse(CoreQuestsModule.isHeldItem(
                items, new ItemStack(Material.WOODEN_AXE), CoreQuestsModule.WOOD_TOOL_ID));
    }

    @Test
    void toolQuestSkipsBoneMealTreesButCountsNaturalOnes() {
        BlockTracker tracker = plugin.context().get(BlockTracker.class);
        List<QuestActionEvent> seen = new ArrayList<>();
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void on(QuestActionEvent event) {
                seen.add(event);
            }
        }, plugin);
        TreeQuestListener listener =
                new TreeQuestListener(server, plugin.context().get(SaplingPlanters.class), tracker);
        Block log = block(Material.JUNGLE_LOG, 60);
        ItemStack axe = new ItemStack(Material.WOODEN_AXE);

        tracker.add(log, BlockTag.GROWN_BONEMEAL);
        listener.onTreeFelled(new TreeFelled(player, log, axe, List.of(log)));
        assertTrue(seen.isEmpty());

        tracker.remove(log, BlockTag.GROWN_BONEMEAL);
        listener.onTreeFelled(new TreeFelled(player, log, axe, List.of(log)));
        assertEquals(1, seen.size());
    }
}
