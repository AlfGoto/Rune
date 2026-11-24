package com.alfgoto.rune.gameplay.traits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.TraitConfig;
import com.alfgoto.rune.gameplay.effects.ScaleRegistry;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class TraitsTest {

    private ServerMock server;
    private RunePlugin plugin;
    private ItemRegistry items;
    private CopperOxidation copper;
    private ToolLoadout loadout;
    private World world;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        // The player joins before the plugin loads: the pack join listener needs a real connection.
        world = server.addSimpleWorld("w");
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        copper = plugin.context().get(CopperOxidation.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack tool(String id) {
        return items.create(items.get(id), 1);
    }

    private double haste(ItemStack tool) {
        return Stacking.total("haste", loadout.sources(tool, player));
    }

    private void breakWith(ItemStack tool, Material type) {
        player.getInventory().setItemInMainHand(tool);
        Block block = world.getBlockAt(1, 64, 1);
        block.setType(type);
        server.getPluginManager().callEvent(new BlockBreakEvent(block, player));
    }

    @Test
    void theFourToolsAreRegisteredWithTheirStats() {
        assertEquals(Rarity.UNCOMMON, items.get("copper_pickaxe").rarity());
        assertEquals(Rarity.UNCOMMON, items.get("copper_axe").rarity());
        assertEquals(Rarity.RARE, items.get("redstone_pickaxe").rarity());
        assertEquals(Rarity.RARE, items.get("breeze_axe").rarity());
        // Capacity: Uncommon 6, Rare 10 - 2 for Redstone and Breeze.
        assertEquals(6, loadout.capacity(tool("copper_pickaxe")));
        assertEquals(6, loadout.capacity(tool("copper_axe")));
        assertEquals(8, loadout.capacity(tool("redstone_pickaxe")));
        assertEquals(8, loadout.capacity(tool("breeze_axe")));
    }

    @Test
    void theScalesAreRegistered() {
        ScaleRegistry scales = plugin.context().get(ScaleRegistry.class);
        assertTrue(scales.find("copper_stage").isPresent());
        assertTrue(scales.find("momentum").isPresent());
    }

    @Test
    void copperStageComesFromBlocksBroken() {
        assertEquals(0, CopperOxidation.stageAt(0, 300));
        assertEquals(0, CopperOxidation.stageAt(299.9, 300));
        assertEquals(1, CopperOxidation.stageAt(300, 300));
        assertEquals(2, CopperOxidation.stageAt(600, 300));
        assertEquals(3, CopperOxidation.stageAt(900, 300));
        assertEquals(3, CopperOxidation.stageAt(5000, 300));
    }

    @Test
    void copperHasteIsZeroTenTwentyThirtyPercent() {
        ItemStack pickaxe = tool("copper_pickaxe");
        double[] expected = {0, 10, 20, 30};
        for (int stage = 0; stage < expected.length; stage++) {
            assertTrue(copper.setStage(pickaxe, stage));
            assertEquals(expected[stage], haste(pickaxe), 1e-9, "stage " + stage);
        }
    }

    @Test
    void copperModelFollowsTheStageAndTheWax() {
        ItemStack axe = tool("copper_axe");
        assertEquals(key("copper_axe"), axe.getItemMeta().getItemModel());
        copper.setStage(axe, 1);
        assertEquals(key("exposed_copper_axe"), axe.getItemMeta().getItemModel());
        copper.setStage(axe, 2);
        assertEquals(key("weathered_copper_axe"), axe.getItemMeta().getItemModel());
        copper.setStage(axe, 3);
        assertEquals(key("oxidized_copper_axe"), axe.getItemMeta().getItemModel());
        assertTrue(copper.wax(axe));
        assertEquals(key("waxed_oxidized_copper_axe"), axe.getItemMeta().getItemModel());
    }

    @Test
    void everyCopperModelHasAnItemDefinition() {
        for (String tool : new String[] {"copper_pickaxe", "copper_axe"}) {
            RuneItem item = items.get(tool);
            for (int stage = 0; stage <= CopperOxidation.LAST_STAGE; stage++) {
                for (boolean waxed : new boolean[] {false, true}) {
                    String model = CopperOxidation.model(item, stage, waxed);
                    assertNotNull(
                            getClass().getResource("/pack/assets/rune/items/" + model + ".json"), model);
                }
            }
        }
    }

    @Test
    void breakingBlocksOxidizesTheToolUntilWaxed() {
        ItemStack pickaxe = tool("copper_pickaxe");
        breakWith(pickaxe, Material.STONE);
        assertEquals(1, copper.blocks(player.getInventory().getItemInMainHand()), 1e-9);

        ItemStack held = player.getInventory().getItemInMainHand();
        assertTrue(copper.wax(held));
        breakWith(held, Material.STONE);
        assertEquals(1, copper.blocks(player.getInventory().getItemInMainHand()), 1e-9, "waxed: no more aging");
    }

    @Test
    void placedBlocksDoNotAgeTheTool() {
        ItemStack pickaxe = tool("copper_pickaxe");
        player.getInventory().setItemInMainHand(pickaxe);
        Block block = world.getBlockAt(1, 64, 1);
        server.getPluginManager()
                .callEvent(new org.bukkit.event.block.BlockPlaceEvent(
                        block,
                        block.getState(),
                        world.getBlockAt(1, 63, 1),
                        new ItemStack(Material.STONE),
                        player,
                        true,
                        org.bukkit.inventory.EquipmentSlot.HAND));
        block.setType(Material.STONE);
        server.getPluginManager().callEvent(new BlockBreakEvent(block, player));
        assertEquals(0, copper.blocks(player.getInventory().getItemInMainHand()), 1e-9);
    }

    @Test
    void anOxidizedToolStopsCounting() {
        ItemStack pickaxe = tool("copper_pickaxe");
        copper.setStage(pickaxe, 3);
        assertFalse(copper.add(pickaxe, 1));
        assertEquals(900, copper.blocks(pickaxe), 1e-9);
    }

    @Test
    void waxNeedsAnUnwaxedCopperToolAndKeepsTheStage() {
        ItemStack pickaxe = tool("copper_pickaxe");
        copper.setStage(pickaxe, 2);
        assertTrue(copper.wax(pickaxe));
        assertTrue(copper.waxed(pickaxe));
        assertEquals(2, copper.stage(pickaxe));
        assertFalse(copper.wax(pickaxe), "already waxed");
        assertFalse(copper.wax(tool("redstone_pickaxe")), "not a Copper tool");
    }

    @Test
    void theWaxRecipeIsRegistered() {
        assertNotNull(server.getRecipe(new NamespacedKey(plugin, WaxListener.RECIPE_KEY)));
    }

    @Test
    void redstoneAndBreezeHasteFollowMomentum() {
        Momentum momentum = plugin.context().get(Momentum.class);
        ItemStack pickaxe = tool("redstone_pickaxe");
        assertEquals(0, haste(pickaxe), 1e-9);
        for (int i = 0; i < 4; i++) {
            momentum.hit(player.getUniqueId());
        }
        assertEquals(20, haste(pickaxe), 1e-9, "4 blocks = +20%");
        for (int i = 0; i < 20; i++) {
            momentum.hit(player.getUniqueId());
        }
        assertEquals(50, haste(tool("breeze_axe")), 1e-9, "capped at +50%");
    }

    @Test
    void redstoneCountsEveryBlockAndBreezeOnlyLogs() {
        Momentum momentum = plugin.context().get(Momentum.class);
        breakWith(tool("redstone_pickaxe"), Material.STONE);
        breakWith(tool("redstone_pickaxe"), Material.DIRT);
        assertEquals(2, momentum.blocks(player.getUniqueId()));
        momentum.forget(player.getUniqueId());
        breakWith(tool("breeze_axe"), Material.STONE);
        assertEquals(0, momentum.blocks(player.getUniqueId()));
        breakWith(tool("breeze_axe"), Material.OAK_LOG);
        breakWith(tool("breeze_axe"), Material.OAK_LOG);
        assertEquals(2, momentum.blocks(player.getUniqueId()));
    }

    @Test
    void momentumResetsAfterTwoSecondsWithoutABreak() {
        AtomicLong now = new AtomicLong(1_000);
        Momentum momentum = new Momentum(TraitConfig.defaults(), now::get);
        var id = player.getUniqueId();
        momentum.hit(id);
        now.addAndGet(1_500);
        momentum.hit(id);
        assertEquals(2, momentum.blocks(id));
        now.addAndGet(2_000);
        assertEquals(2, momentum.blocks(id), "exactly 2 s is still in a row");
        now.addAndGet(1);
        assertEquals(0, momentum.blocks(id));
        momentum.hit(id);
        assertEquals(1, momentum.blocks(id), "a new streak starts at 1");
    }

    @Test
    void momentumIsCappedAtTenBlocks() {
        Momentum momentum = new Momentum(TraitConfig.defaults(), () -> 0);
        var id = player.getUniqueId();
        for (int i = 0; i < 25; i++) {
            momentum.hit(id);
        }
        assertEquals(10, momentum.blocks(id));
        assertEquals(1.0, momentum.fraction(id), 1e-9);
    }

    @Test
    void recipesUseTheDocumentedIngredients() {
        for (String id : new String[] {"copper_pickaxe", "copper_axe", "redstone_pickaxe", "breeze_axe"}) {
            assertNotNull(server.getRecipe(plugin.context().get(com.alfgoto.rune.gameplay.crafting.RecipeBook.class).key(id)), id);
        }
    }

    private static NamespacedKey key(String model) {
        return new NamespacedKey("rune", model);
    }
}
