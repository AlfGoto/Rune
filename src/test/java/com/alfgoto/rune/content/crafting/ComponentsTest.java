package com.alfgoto.rune.content.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class ComponentsTest {

    private static final List<String> IDS = List.of(
            "reinforced_handle",
            "reinforced_lining",
            "gem_setting",
            "echo_lens",
            "deep_core",
            "abyssal_plate",
            "legendary_core");

    private ServerMock server;
    private ItemRegistry items;
    private RecipeBook book;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        MockBukkit.getMock().getScheduler().performOneTick(); // Rune recipes reach the server one tick later
        items = plugin.context().get(ItemRegistry.class);
        book = plugin.context().get(RecipeBook.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void allSevenComponentsAreItemsWithARecipe() {
        for (String id : IDS) {
            assertTrue(items.find(id).isPresent(), id + " item");
            assertTrue(book.find(id).isPresent(), id + " recipe");
            assertNotNull(server.getRecipe(book.key(id)), id + " server recipe");
        }
    }

    @Test
    void raritiesFollowWhatUsesThem() {
        assertEquals(Rarity.UNCOMMON, items.get("reinforced_handle").rarity());
        assertEquals(Rarity.RARE, items.get("gem_setting").rarity());
        assertEquals(Rarity.EPIC, items.get("abyssal_plate").rarity());
        assertEquals(Rarity.LEGENDARY, items.get("legendary_core").rarity());
    }

    @Test
    void recipesUseTheIngredientsOfTheDocs() {
        assertEquals(
                Map.of(Material.STICK, 2L, Material.IRON_INGOT, 1L, Material.LEATHER, 1L),
                counts("reinforced_handle"));
        assertEquals(
                Map.of(Material.LEATHER, 2L, Material.WHITE_WOOL, 1L, Material.IRON_INGOT, 1L),
                counts("reinforced_lining"));
        assertEquals(Map.of(Material.GOLD_INGOT, 1L, Material.GOLD_NUGGET, 4L), counts("gem_setting"));
        assertEquals(
                Map.of(Material.ECHO_SHARD, 4L, Material.AMETHYST_SHARD, 1L, Material.TINTED_GLASS, 1L),
                counts("echo_lens"));
        assertEquals(
                Map.of(Material.DEEPSLATE, 4L, Material.AMETHYST_SHARD, 4L, Material.DIAMOND, 1L),
                counts("deep_core"));
        assertEquals(
                Map.of(Material.OBSIDIAN, 4L, Material.CRYING_OBSIDIAN, 4L, Material.NETHERITE_SCRAP, 1L),
                counts("abyssal_plate"));
        assertEquals(
                Map.of(
                        Material.NETHER_STAR, 1L,
                        Material.DRAGON_HEAD, 1L,
                        Material.NETHERITE_INGOT, 1L,
                        Material.DIAMOND_BLOCK, 4L,
                        Material.ECHO_SHARD, 2L),
                counts("legendary_core"));
    }

    private Map<Material, Long> counts(String id) {
        ShapedRecipe recipe = (ShapedRecipe) server.getRecipe(book.key(id));
        Map<Material, Long> counts = new java.util.HashMap<>();
        for (String row : recipe.getShape()) {
            for (char c : row.toCharArray()) {
                if (c != ' ') {
                    Material type = recipe.getIngredientMap().get(c).getType();
                    counts.merge(type, 1L, Long::sum);
                }
            }
        }
        return counts;
    }
}
