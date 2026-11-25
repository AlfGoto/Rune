package com.alfgoto.rune.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.overrides.LockedGearRecipes;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

/** Issue #228: Rune recipes are built after every redrawer exists, and Locked Gear recipes are removed. */
class DeferredRecipeTest {

    private ServerMock server;
    private RunePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void deferredRecipeResultHasTheNameFromARedrawerAddedAfterRegister() {
        ItemRegistry items = new ItemRegistry(plugin, ToolConfig.defaults());
        items.register(new RuneItem("gear", Rarity.UNCOMMON, Material.STICK, "test_rune"));
        JobProgress progress = new JobProgress(new NoStorage(), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        RecipeBook book = new RecipeBook(plugin, items, progress);
        book.deferRegistration();
        book.register(new CraftingRecipe(
                "deferred_gear", Job.builder("mining").build(), 1, "gear", 1,
                List.of("II"), Map.of('I', Ingredient.vanilla(Material.IRON_INGOT))));
        assertNull(server.getRecipe(book.key("deferred_gear")));

        items.addRedrawer((stack, item) -> stack.editMeta(meta -> meta.itemName(Component.text("Rune Name"))));
        book.flush();

        ShapedRecipe shaped = (ShapedRecipe) server.getRecipe(book.key("deferred_gear"));
        assertNotNull(shaped);
        assertEquals(Component.text("Rune Name"), shaped.getResult().getItemMeta().itemName());
    }

    @Test
    void lockedGearRecipesAreRemovedAndOthersKept() {
        NamespacedKey ironPick = key(Material.IRON_PICKAXE);
        NamespacedKey diamondChest = key(Material.DIAMOND_CHESTPLATE);
        server.addRecipe(new ShapedRecipe(ironPick, new org.bukkit.inventory.ItemStack(Material.IRON_PICKAXE))
                .shape("I").setIngredient('I', Material.IRON_INGOT));
        server.addRecipe(new ShapedRecipe(diamondChest, new org.bukkit.inventory.ItemStack(Material.DIAMOND_CHESTPLATE))
                .shape("D").setIngredient('D', Material.DIAMOND));
        NamespacedKey wooden = key(Material.WOODEN_PICKAXE);
        server.addRecipe(new ShapedRecipe(wooden, new org.bukkit.inventory.ItemStack(Material.WOODEN_PICKAXE))
                .shape("P").setIngredient('P', Material.OAK_PLANKS));
        NamespacedKey table = key(Material.CRAFTING_TABLE);
        server.addRecipe(new ShapedRecipe(table, new org.bukkit.inventory.ItemStack(Material.CRAFTING_TABLE))
                .shape("P").setIngredient('P', Material.OAK_PLANKS));

        LockedGearRecipes.removeAll(server);

        assertNull(server.getRecipe(ironPick));
        assertNull(server.getRecipe(diamondChest));
        assertNotNull(server.getRecipe(wooden));
        assertNotNull(server.getRecipe(table));
    }

    private NamespacedKey key(Material material) {
        return new NamespacedKey(plugin, "test_" + material.name().toLowerCase());
    }

    private static final class NoStorage implements PlayerStorage {
        @Override
        public Optional<String> get(UUID player, String key) {
            return Optional.empty();
        }

        @Override
        public void set(UUID player, String key, String value) {}

        @Override
        public void remove(UUID player, String key) {}
    }
}
