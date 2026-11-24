package com.alfgoto.rune.content.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
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

class StarterAxesTest {

    private RunePlugin plugin;
    private ServerMock server;
    private ItemRegistry items;
    private RecipeBook book;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        book = plugin.context().get(RecipeBook.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack axe(String id) {
        return items.create(items.get(id), 1);
    }

    @Test
    void raritiesLevelsAndRecipesExist() {
        assertEquals(Rarity.COMMON, items.get("flint_hatchet").rarity());
        assertEquals(Rarity.COMMON, items.get("stone_hatchet").rarity());
        assertEquals(Rarity.UNCOMMON, items.get("woodsmans_axe").rarity());
        assertEquals(2, book.find("flint_hatchet").orElseThrow().level());
        assertEquals(2, book.find("stone_hatchet").orElseThrow().level());
        assertEquals(4, book.find("copper_axe").orElseThrow().level());
        assertEquals(5, book.find("woodsmans_axe").orElseThrow().level());
        for (String id : new String[] {"flint_hatchet", "stone_hatchet", "copper_axe", "woodsmans_axe"}) {
            assertNotNull(server.getRecipe(book.key(id)), id);
        }
    }

    @Test
    void recipesUseTheIngredientsOfTheDocs() {
        assertEquals(Map.of(Material.FLINT, 3L, Material.STICK, 2L), counts("flint_hatchet"));
        assertEquals(
                Map.of(Material.COBBLESTONE, 2L, Material.FLINT, 1L, Material.STICK, 2L), counts("stone_hatchet"));
    }

    @Test
    void capacityFollowsTheDocs() {
        assertEquals(3, loadout.capacity(axe("flint_hatchet")));
        assertEquals(2, loadout.capacity(axe("stone_hatchet")));
        assertEquals(8, loadout.capacity(axe("woodsmans_axe")));
    }

    @Test
    void traitsGiveTheirEffects() {
        assertEquals(15, Stacking.total("fortune", loadout.sources(axe("flint_hatchet"))));
        assertEquals(-10, Stacking.total("haste", loadout.sources(axe("flint_hatchet"))));
        assertEquals(15, Stacking.total("haste", loadout.sources(axe("stone_hatchet"))));
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
