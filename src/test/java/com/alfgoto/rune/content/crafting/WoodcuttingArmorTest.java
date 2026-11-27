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
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.woodcutting.Rhythm;
import com.alfgoto.rune.gameplay.woodcutting.RhythmRules;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class WoodcuttingArmorTest {

    private static final List<String> COMMON =
            List.of("lumberjacks_cap", "lumberjacks_vest", "lumberjacks_trousers", "lumberjacks_boots");
    private static final List<String> UNCOMMON =
            List.of("fellers_hood", "fellers_jerkin", "fellers_leggings", "fellers_boots");

    private ServerMock server;
    private ItemRegistry items;
    private RecipeBook book;
    private ToolLoadout loadout;
    private Rhythm rhythm;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        book = plugin.context().get(RecipeBook.class);
        loadout = plugin.context().get(ToolLoadout.class);
        rhythm = plugin.context().get(Rhythm.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void eightPiecesAreCraftableWithRarityKindAndLevel() {
        List<ArmorKind> kinds = List.of(ArmorKind.HELMET, ArmorKind.CHESTPLATE, ArmorKind.LEGGINGS, ArmorKind.BOOTS);
        for (int i = 0; i < 4; i++) {
            for (String id : List.of(COMMON.get(i), UNCOMMON.get(i))) {
                RuneItem item = items.get(id);
                assertEquals(kinds.get(i), item.armorSpec().orElseThrow().kind(), id);
                assertNotNull(server.getRecipe(book.key(id)), id + " server recipe");
            }
            assertEquals(Rarity.COMMON, items.get(COMMON.get(i)).rarity());
            assertEquals(Rarity.UNCOMMON, items.get(UNCOMMON.get(i)).rarity());
            assertEquals(3, book.find(COMMON.get(i)).orElseThrow().level());
            assertEquals(7, book.find(UNCOMMON.get(i)).orElseThrow().level());
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

    @Test
    void piecesWearTheWoodcuttingLayer() {
        assertEquals("rune:woodcutting_common", items.get("lumberjacks_cap").armorSpec().orElseThrow().equipment());
        assertEquals("rune:woodcutting_uncommon", items.get("fellers_boots").armorSpec().orElseThrow().equipment());
    }

    @Test
    void traitsFollowTheChargesAndStackAcrossPieces() {
        Player player = server.addPlayer();
        player.getInventory().setHelmet(items.create(items.get("fellers_hood"), 1));
        player.getInventory().setBoots(items.create(items.get("lumberjacks_boots"), 1));

        assertEquals(0, loadout.wornTotal(player, RhythmRules.CHOP_SPEED), 1e-9, "no charge, no bonus");
        for (int i = 0; i < 5; i++) {
            rhythm.add(player.getUniqueId());
        }
        assertEquals(12.5, loadout.wornTotal(player, RhythmRules.CHOP_SPEED), 1e-9, "uncommon 25% x 5/10");
        assertEquals(5, loadout.wornTotal(player, RhythmRules.JUMP), 1e-9, "common 10% x 5/10");
        assertEquals(10, loadout.wornTotal(player, RhythmRules.FALL_REDUCTION), 1e-9, "common 20% x 5/10");
        for (int i = 0; i < 10; i++) {
            rhythm.add(player.getUniqueId());
        }
        assertEquals(25, loadout.wornTotal(player, RhythmRules.CHOP_SPEED), 1e-9, "full charges");
        assertEquals(0, loadout.wornTotal(player, RhythmRules.DAMAGE_REDUCTION), 1e-9, "no chestplate");
    }
}
