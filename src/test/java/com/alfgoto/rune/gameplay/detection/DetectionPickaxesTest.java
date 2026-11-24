package com.alfgoto.rune.gameplay.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;
import com.alfgoto.rune.gameplay.runes.StoneFamily;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class DetectionPickaxesTest {

    private ServerMock server;
    private RunePlugin plugin;
    private ItemRegistry items;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack tool(String id) {
        return items.create(items.get(id), 1);
    }

    @Test
    void boneGivesOreSenseChance() {
        ItemStack bone = tool("bone_pickaxe");
        assertEquals(Rarity.UNCOMMON, items.get("bone_pickaxe").rarity());
        assertEquals(5, Stacking.total("ore_sense", loadout.sources(bone)));
        assertEquals(6, loadout.capacity(bone));
        assertEquals(0, Stacking.total("echo_ping", loadout.sources(bone)));
    }

    @Test
    void echoPingsAndHasDeepDarkAffinityAndPlusTwoCapacity() {
        ItemStack echo = tool("echo_pickaxe");
        assertEquals(Rarity.RARE, items.get("echo_pickaxe").rarity());
        assertTrue(Stacking.total("echo_ping", loadout.sources(echo)) > 0);
        assertEquals(10, Stacking.total("affinity_deep_dark", loadout.sources(echo)));
        assertEquals(12, loadout.capacity(echo));
    }

    @Test
    void recipesNeedMining7And15() {
        RecipeBook book = plugin.context().get(RecipeBook.class);
        assertEquals(7, book.find("bone_pickaxe").orElseThrow().level());
        assertEquals(15, book.find("echo_pickaxe").orElseThrow().level());
        assertNotNull(server.getRecipe(book.key("echo_pickaxe")));
    }

    @Test
    void deepDarkPlaceExists() {
        assertTrue(plugin.context().get(PlaceRegistry.class).all().stream().anyMatch(p -> p.id().equals("deep_dark")));
    }

    @Test
    void stoneFamilyAndOres() {
        assertTrue(StoneFamily.isStone(Material.TUFF));
        assertTrue(StoneFamily.isStone(Material.NETHERRACK));
        assertFalse(StoneFamily.isStone(Material.COBBLESTONE));
        assertTrue(StoneFamily.isOre(Material.DEEPSLATE_IRON_ORE));
        assertTrue(StoneFamily.isOre(Material.ANCIENT_DEBRIS));
        assertFalse(StoneFamily.isOre(Material.STONE));
    }
}
