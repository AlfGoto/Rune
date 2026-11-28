package com.alfgoto.rune.gameplay.drops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.items.TraitBinding;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** The Loot line: Magnetite, Geode and Ender pickaxes. */
class LootPickaxesTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        MockBukkit.getMock().getScheduler().performOneTick(); // Rune recipes reach the server one tick later
        world = server.addSimpleWorld("loot");
        items = plugin.context().get(ItemRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private BlockDropItemEvent breakWith(ItemStack tool, Material drop, int amount) {
        player.getInventory().setItemInMainHand(tool);
        Block block = world.getBlockAt(0, 10, 0);
        Item dropped = world.dropItem(block.getLocation(), new ItemStack(drop, amount));
        BlockDropItemEvent event = new BlockDropItemEvent(block, block.getState(), player, new ArrayList<>(List.of(dropped)));
        server.getPluginManager().callEvent(event);
        return event;
    }

    private static int count(org.bukkit.inventory.Inventory inventory, Material material) {
        int total = 0;
        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    @Test
    void rarityTierAndRecipeLevels() {
        RecipeBook book = plugin.context().get(RecipeBook.class);
        assertEquals(Rarity.UNCOMMON, items.get("magnetite_pickaxe").rarity());
        assertEquals(Rarity.RARE, items.get("geode_pickaxe").rarity());
        assertEquals(Rarity.EPIC, items.get("ender_pickaxe").rarity());
        assertEquals(8, book.find("magnetite_pickaxe").orElseThrow().level());
        assertEquals(12, book.find("geode_pickaxe").orElseThrow().level());
        assertEquals(22, book.find("ender_pickaxe").orElseThrow().level());
        for (String id : new String[] {"magnetite_pickaxe", "geode_pickaxe", "ender_pickaxe"}) {
            assertNotNull(server.getRecipe(book.key(id)), id);
        }
    }

    @Test
    void traitsGiveTheirEffects() {
        var magnetite = loadout.sources(items.create(items.get("magnetite_pickaxe"), 1));
        var geode = loadout.sources(items.create(items.get("geode_pickaxe"), 1));
        var ender = loadout.sources(items.create(items.get("ender_pickaxe"), 1));
        assertEquals(4, Stacking.total("magnet", magnetite));
        assertEquals(15, Stacking.total("bonus_gems", geode));
        assertEquals(1, Stacking.total("ender_drops", ender));
    }

    @Test
    void magnetiteSendsTheDropsToTheInventory() {
        breakWith(items.create(items.get("magnetite_pickaxe"), 1), Material.IRON_INGOT, 2);
        assertEquals(2, count(player.getInventory(), Material.IRON_INGOT));
    }

    @Test
    void enderSendsTheDropsToTheEnderChest() {
        BlockDropItemEvent event = breakWith(items.create(items.get("ender_pickaxe"), 1), Material.DIAMOND, 1);
        assertEquals(1, count(player.getEnderChest(), Material.DIAMOND));
        assertEquals(0, count(player.getInventory(), Material.DIAMOND));
        assertFalse(event.getItems().get(0).isValid()); // taken out of the world
    }

    @Test
    void geodeAddsAGemAndLeavesOtherDropsAlone() {
        ItemStack sure = items.create(
                items.register(RuneItem.tool(
                        "sure_geode", Rarity.RARE, ToolKind.PICKAXE, "test_rune",
                        ToolSpec.DEFAULT.withTrait("test_geode", 0, TraitBinding.always("bonus_gems", 100)))),
                1);
        BlockDropItemEvent gem = breakWith(sure, Material.DIAMOND, 1);
        assertEquals(2, gem.getItems().get(0).getItemStack().getAmount());
        BlockDropItemEvent stone = breakWith(sure, Material.COBBLESTONE, 1);
        assertEquals(1, stone.getItems().get(0).getItemStack().getAmount());
    }

    @Test
    void geodeNeedsARuneTool() {
        BlockDropItemEvent event = breakWith(new ItemStack(Material.DIAMOND_PICKAXE), Material.DIAMOND, 1);
        assertEquals(1, event.getItems().get(0).getItemStack().getAmount());
    }
}
