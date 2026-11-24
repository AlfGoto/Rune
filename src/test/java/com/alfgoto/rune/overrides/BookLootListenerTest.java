package com.alfgoto.rune.overrides;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.content.caches.CoreCachesModule;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class BookLootListenerTest {

    private BookLootListener listener;
    private Caches caches;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        ItemRegistry items = new ItemRegistry(plugin);
        CoreCachesModule.items().forEach(items::register);
        caches = new Caches(plugin, items, null, null, new java.util.Random(1));
        listener = new BookLootListener(caches, Rarity.RARE);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void enchantedBookBecomesACacheOfTheSameAmount() {
        ItemStack result = listener.replace(new ItemStack(Material.ENCHANTED_BOOK, 2));
        assertEquals(Rarity.RARE, caches.rarityOf(result).orElseThrow());
        assertEquals(2, result.getAmount());
    }

    @Test
    void otherItemsAreKept() {
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        assertSame(diamond, listener.replace(diamond));
    }

    @Test
    void enchantedBookTradeSellsACacheForTheSamePrice() {
        MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.ENCHANTED_BOOK), 5);
        recipe.setIngredients(List.of(new ItemStack(Material.EMERALD, 12), new ItemStack(Material.BOOK)));
        MerchantRecipe replaced = listener.replace(recipe);
        assertEquals(Rarity.RARE, caches.rarityOf(replaced.getResult()).orElseThrow());
        assertEquals(5, replaced.getMaxUses());
        assertEquals(recipe.getIngredients(), replaced.getIngredients());
    }

    @Test
    void otherTradesAreKept() {
        MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.BREAD), 5);
        assertSame(recipe, listener.replace(recipe));
    }
}
