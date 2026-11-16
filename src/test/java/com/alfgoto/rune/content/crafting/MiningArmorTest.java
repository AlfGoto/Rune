package com.alfgoto.rune.content.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ArmorKind;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class MiningArmorTest {

    private static final List<String> COMMON = List.of("miners_helmet", "miners_vest", "miners_trousers", "miners_boots");
    private static final List<String> UNCOMMON =
            List.of("cavers_helmet", "reinforced_chestplate", "cavers_leggings", "cavers_boots");

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
    void eightPiecesAreCraftable() {
        for (String id : concat(COMMON, UNCOMMON)) {
            assertTrue(items.get(id).armorSpec().isPresent(), id + " is Rune Armor");
            assertNotNull(server.getRecipe(book.key(id)), id + " server recipe");
        }
    }

    @Test
    void raritiesAndSlotsAreRight() {
        List<ArmorKind> kinds = List.of(ArmorKind.HELMET, ArmorKind.CHESTPLATE, ArmorKind.LEGGINGS, ArmorKind.BOOTS);
        for (int i = 0; i < 4; i++) {
            RuneItem common = items.get(COMMON.get(i));
            RuneItem uncommon = items.get(UNCOMMON.get(i));
            assertEquals(Rarity.COMMON, common.rarity());
            assertEquals(Rarity.UNCOMMON, uncommon.rarity());
            assertEquals(kinds.get(i), common.armorSpec().orElseThrow().kind());
            assertEquals(kinds.get(i), uncommon.armorSpec().orElseThrow().kind());
        }
    }

    @Test
    void unlockLevelsAre3And7() {
        for (String id : COMMON) {
            assertEquals(3, book.find(id).orElseThrow().level(), id);
        }
        for (String id : UNCOMMON) {
            assertEquals(7, book.find(id).orElseThrow().level(), id);
        }
    }

    @Test
    void everyUncommonPieceConsumesItsCommonPieceAndALining() {
        for (int i = 0; i < 4; i++) {
            CraftingRecipe recipe = book.find(UNCOMMON.get(i)).orElseThrow();
            String common = COMMON.get(i);
            assertTrue(recipe.ingredients().values().stream().anyMatch(ing -> common.equals(ing.itemId())));
            assertTrue(recipe.ingredients().values().stream()
                    .anyMatch(ing -> "reinforced_lining".equals(ing.itemId())));
        }
    }

    private static List<String> concat(List<String> a, List<String> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }
}
