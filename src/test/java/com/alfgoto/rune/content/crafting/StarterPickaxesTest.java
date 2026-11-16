package com.alfgoto.rune.content.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class StarterPickaxesTest {

    private ServerMock server;
    private ItemRegistry items;
    private RecipeBook book;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        book = plugin.context().get(RecipeBook.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void flintAndCoalAreCommonPickaxesUnlockedAtMining2() {
        for (String id : new String[] {"flint_pickaxe", "coal_pickaxe"}) {
            assertEquals(Rarity.COMMON, items.get(id).rarity(), id);
            assertEquals(2, book.find(id).orElseThrow().level(), id);
            assertNotNull(server.getRecipe(book.key(id)), id);
        }
    }

    @Test
    void recipesUseTheIngredientsOfTheDocs() {
        assertEquals(Map.of(Material.FLINT, 3L, Material.STICK, 2L), counts("flint_pickaxe"));
        assertEquals(
                Map.of(Material.COAL, 2L, Material.COBBLESTONE, 1L, Material.STICK, 2L), counts("coal_pickaxe"));
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
