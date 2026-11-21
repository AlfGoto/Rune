package com.alfgoto.rune.content.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.quests.QuestType;
import com.alfgoto.rune.gameplay.quests.QuestTypeRegistry;
import com.alfgoto.rune.text.Rarity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
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
        assertFalse(place.objective().counts(new JobActionEvent(player, CoreJobs.MINING, block(Material.STONE, 60), null)));
    }
}
