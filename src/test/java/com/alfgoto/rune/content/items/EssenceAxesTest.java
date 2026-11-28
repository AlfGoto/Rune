package com.alfgoto.rune.content.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.woodcutting.AxeTraits;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class EssenceAxesTest {

    private RunePlugin plugin;
    private ServerMock server;
    private ItemRegistry items;
    private RecipeBook book;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        MockBukkit.getMock().getScheduler().performOneTick(); // Rune recipes reach the server one tick later
        items = plugin.context().get(ItemRegistry.class);
        book = plugin.context().get(RecipeBook.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private double total(String axe, String effect) {
        ItemStack stack = items.create(items.get(axe), 1);
        return Stacking.total(effect, loadout.sources(stack));
    }

    @Test
    void raritiesAndCapacityFollowTheDocs() {
        for (String id : new String[] {"birch_axe", "spruce_axe", "poplar_axe"}) {
            assertEquals(Rarity.UNCOMMON, items.get(id).rarity(), id);
            assertEquals(6, loadout.capacity(items.create(items.get(id), 1)), id);
        }
        assertEquals(Rarity.RARE, items.get("jungle_axe").rarity());
        assertEquals(10, loadout.capacity(items.create(items.get("jungle_axe"), 1)));
    }

    @Test
    void everyAxeHasTheAffinityTraitOfItsPlaceAtTheRankOneValue() {
        assertEquals(10, total("birch_axe", "affinity_birch_forest"));
        assertEquals(10, total("spruce_axe", "affinity_taiga"));
        assertEquals(10, total("poplar_axe", "affinity_dappled_forest"));
        assertEquals(10, total("jungle_axe", "affinity_jungle"));
    }

    @Test
    void eachAxeHasItsExtraWithTheConfigNumbers() {
        assertEquals(5, total("birch_axe", AxeTraits.FELLED_TREE_SPEED));
        assertEquals(1, total("spruce_axe", AxeTraits.POWDER_SNOW_IMMUNITY));
        assertEquals(1, total("poplar_axe", AxeTraits.FELLED_TREE_LEAF_FALL));
        assertEquals(50, total("jungle_axe", AxeTraits.CLIMB_SPEED));
        assertEquals(0, total("birch_axe", AxeTraits.CLIMB_SPEED));
    }

    @Test
    void theNewPlacesExist() {
        PlaceRegistry places = plugin.context().get(PlaceRegistry.class);
        Map<String, Integer> keys = new HashMap<>();
        places.all().forEach(p -> keys.put(p.id(), p.keys().size()));
        assertEquals(2, keys.get("birch_forest"));
        assertEquals(1, keys.get("dappled_forest"));
        assertEquals(3, keys.get("jungle"));
    }

    @Test
    void recipesUseTheIngredientsOfTheDocs() {
        assertEquals(6, book.find("birch_axe").orElseThrow().level());
        assertEquals(6, book.find("spruce_axe").orElseThrow().level());
        assertEquals(7, book.find("poplar_axe").orElseThrow().level());
        assertEquals(10, book.find("jungle_axe").orElseThrow().level());
        // Components are paper items in the recipe grid (Reinforced Handle, Gem Setting).
        assertEquals(Map.of(Material.BIRCH_LOG, 4L, Material.FEATHER, 1L, Material.PAPER, 1L), counts("birch_axe"));
        assertEquals(
                Map.of(Material.SPRUCE_LOG, 4L, Material.POWDER_SNOW_BUCKET, 1L, Material.PAPER, 1L),
                counts("spruce_axe"));
        assertEquals(
                Map.of(Material.JUNGLE_LOG, 4L, Material.COCOA_BEANS, 2L, Material.DIAMOND, 1L, Material.PAPER, 1L),
                counts("jungle_axe"));
        assertNotNull(server.getRecipe(book.key("poplar_axe")));
    }

    @Test
    void crimsonWarpedAcaciaAndCherryHaveTheirAffinityAndExtra() {
        for (String id : new String[] {"crimson_axe", "warped_axe", "acacia_axe", "cherry_axe"}) {
            assertEquals(Rarity.RARE, items.get(id).rarity(), id);
            assertEquals(10, loadout.capacity(items.create(items.get(id), 1)), id);
        }
        assertEquals(10, total("crimson_axe", "affinity_crimson_forest"));
        assertEquals(10, total("warped_axe", "affinity_warped_forest"));
        assertEquals(10, total("acacia_axe", "affinity_savanna"));
        assertEquals(10, total("cherry_axe", "affinity_cherry_grove"));
        assertEquals(8, total("crimson_axe", AxeTraits.STEM_FIRE_RESISTANCE));
        assertEquals(1, total("warped_axe", AxeTraits.ENDERMEN_NEUTRAL));
        assertEquals(20, total("acacia_axe", AxeTraits.SUN_RAIN_SPEED));
        assertEquals(6, total("cherry_axe", AxeTraits.FELLED_TREE_REGENERATION));
        assertEquals(0, total("cherry_axe", AxeTraits.STEM_FIRE_RESISTANCE));
    }

    @Test
    void theNewForestPlacesAndRecipesExist() {
        PlaceRegistry places = plugin.context().get(PlaceRegistry.class);
        for (String id : new String[] {"crimson_forest", "warped_forest", "savanna", "cherry_grove"}) {
            assertTrue(places.all().stream().anyMatch(p -> p.id().equals(id)), id);
        }
        assertEquals(10, book.find("crimson_axe").orElseThrow().level());
        assertEquals(10, book.find("warped_axe").orElseThrow().level());
        assertEquals(13, book.find("acacia_axe").orElseThrow().level());
        assertEquals(13, book.find("cherry_axe").orElseThrow().level());
        assertEquals(
                Map.of(Material.CRIMSON_STEM, 4L, Material.SHROOMLIGHT, 2L, Material.DIAMOND, 1L, Material.PAPER, 1L),
                counts("crimson_axe"));
        assertEquals(
                Map.of(Material.CHERRY_LOG, 4L, Material.PINK_PETALS, 3L, Material.DIAMOND, 1L, Material.PAPER, 1L),
                counts("cherry_axe"));
    }

    @Test
    void sunRainSpeedRule() {
        assertEquals(20, AxeTraits.sunRainSpeed(false, false, true, 20));
        assertEquals(0, AxeTraits.sunRainSpeed(false, false, false, 20));
        assertEquals(-20, AxeTraits.sunRainSpeed(true, false, true, 20));
        assertEquals(-20, AxeTraits.sunRainSpeed(false, true, true, 20));
    }

    @Test
    void theAxesAreRuneAxes() {
        assertTrue(items.toolStats(items.get("jungle_axe")).isPresent());
    }

    private Map<Material, Long> counts(String id) {
        ShapedRecipe recipe = (ShapedRecipe) server.getRecipe(book.key(id));
        Map<Material, Long> counts = new HashMap<>();
        for (String row : recipe.getShape()) {
            for (char c : row.toCharArray()) {
                if (c != ' ') {
                    counts.merge(recipe.getIngredientMap().get(c).getType(), 1L, Long::sum);
                }
            }
        }
        return counts;
    }
}
