package com.alfgoto.rune.content.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class BiomePickaxesTest {

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

    private double total(String pickaxe, String effect) {
        ItemStack stack = items.create(items.get(pickaxe), 1);
        List<Stacking.Source> sources = loadout.sources(stack);
        return Stacking.total(effect, sources);
    }

    @Test
    void recipesAreUnlockedAtTheDocumentedLevels() {
        RecipeBook book = plugin.context().get(RecipeBook.class);
        assertEquals(6, book.find("lichen_pickaxe").orElseThrow().level());
        assertEquals(10, book.find("fiery_pickaxe").orElseThrow().level());
        assertEquals(13, book.find("emerald_pickaxe").orElseThrow().level());
        assertEquals(14, book.find("prismarine_pickaxe").orElseThrow().level());
        for (String id : List.of("lichen_pickaxe", "fiery_pickaxe", "emerald_pickaxe", "prismarine_pickaxe")) {
            assertNotNull(server.getRecipe(book.key(id)), id);
        }
    }

    @Test
    void raritiesAndLichenCapacity() {
        assertEquals(Rarity.UNCOMMON, items.get("lichen_pickaxe").rarity());
        assertEquals(Rarity.RARE, items.get("fiery_pickaxe").rarity());
        assertEquals(7, loadout.capacity(items.create(items.get("lichen_pickaxe"), 1)));
        assertEquals(10, loadout.capacity(items.create(items.get("fiery_pickaxe"), 1)));
    }

    @Test
    void traitsGiveTheirAffinityAndExtra() {
        assertEquals(10, total("lichen_pickaxe", "affinity_lush_caves"));
        assertEquals(12, total("lichen_pickaxe", "glow"));
        assertEquals(10, total("fiery_pickaxe", "trait_affinity_nether"));
        assertEquals(25, total("fiery_pickaxe", "smelt"));
        assertEquals(10, total("emerald_pickaxe", "trait_affinity_mountains"));
        assertEquals(10, total("emerald_pickaxe", "trait_mountain_move_speed"));
        assertEquals(10, total("prismarine_pickaxe", "trait_affinity_ocean"));
        assertTrue(total("prismarine_pickaxe", "no_water_penalty") > 0);
        assertTrue(total("prismarine_pickaxe", "no_air_penalty") > 0);
    }

    @Test
    void newPlacesExist() {
        PlaceRegistry places = plugin.context().get(PlaceRegistry.class);
        for (String id : List.of("mountains", "ocean", "nether")) {
            assertTrue(places.all().stream().anyMatch(p -> p.id().equals(id)), id);
        }
    }
}
