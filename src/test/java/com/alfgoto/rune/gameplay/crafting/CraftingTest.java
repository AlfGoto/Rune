package com.alfgoto.rune.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class CraftingTest {

    private final Job mining = Job.builder("mining").build();
    private ServerMock server;
    private RunePlugin plugin;
    private JobProgress progress;
    private ItemRegistry items;
    private RecipeBook book;
    private PlayerMock player;
    private CraftingRecipe recipe;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        progress = new JobProgress(new MemoryStorage(), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        items = new ItemRegistry(plugin);
        items.register(new RuneItem("component", Rarity.COMMON, Material.PAPER, "test_rune"));
        items.register(new RuneItem("gear", Rarity.UNCOMMON, Material.STICK, "test_rune"));
        book = new RecipeBook(plugin, items, progress);
        recipe = book.register(new CraftingRecipe(
                "test_gear",
                mining,
                2,
                "gear",
                1,
                List.of("CC", "CI"),
                Map.of('C', Ingredient.rune("component"), 'I', Ingredient.vanilla(Material.IRON_INGOT))));
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void recipeIsLockedUntilTheJobLevel() {
        assertFalse(book.isUnlocked(player.getUniqueId(), recipe));
        progress.setJobLevel(player.getUniqueId(), mining, 2);
        assertTrue(book.isUnlocked(player.getUniqueId(), recipe));
    }

    @Test
    void recipeIsLockedAgainWhenTheLevelGoesDown() {
        progress.setJobLevel(player.getUniqueId(), mining, 3);
        progress.setJobLevel(player.getUniqueId(), mining, 1);
        assertFalse(book.isUnlocked(player.getUniqueId(), recipe));
    }

    @Test
    void registeredRecipeMakesARuneItemAndUsesRuneIngredients() {
        Recipe registered = server.getRecipe(book.key("test_gear"));
        ShapedRecipe shaped = (ShapedRecipe) registered;
        assertEquals("gear", items.of(shaped.getResult()).orElseThrow().id());
        assertEquals(2, shaped.getIngredientMap().values().stream()
                .filter(java.util.Objects::nonNull)
                .map(stack -> stack.getType())
                .distinct()
                .count());
        assertEquals(recipe, book.of(registered).orElseThrow());
    }

    @Test
    void preparedResultOfAnUnlockedRuneRecipeIsDrawn() {
        items.addRedrawer((stack, item) -> stack.editMeta(meta -> meta.setDisplayName("drawn " + item.id())));
        progress.setJobLevel(player.getUniqueId(), mining, 2);
        CraftingInventory inventory = prepare(player);
        assertEquals("gear", items.of(inventory.getResult()).orElseThrow().id());
        assertEquals("drawn gear", inventory.getResult().getItemMeta().getDisplayName());
    }

    @Test
    void preparedResultOfALockedRuneRecipeIsRemoved() {
        assertNull(prepare(player).getResult());
    }

    private CraftingInventory prepare(PlayerMock who) {
        InventoryView view = who.openWorkbench(null, true);
        CraftingInventory inventory = (CraftingInventory) view.getTopInventory();
        inventory.setMatrix(new ItemStack[] {
            items.create(items.get("component"), 1), items.create(items.get("component"), 1),
            items.create(items.get("component"), 1), new ItemStack(Material.IRON_INGOT),
            null, null, null, null, null
        });
        inventory.setResult(items.create(items.get("gear"), 1));
        new CraftingListener(book, items).onPrepare(
                new PrepareItemCraftEvent(inventory, view, false) {
                    @Override
                    public Recipe getRecipe() {
                        return server.getRecipe(book.key("test_gear"));
                    }
                });
        return inventory;
    }

    @Test
    void vanillaRecipesAreNotRuneRecipes() {
        assertTrue(book.of(server.getRecipe(org.bukkit.NamespacedKey.minecraft("stick"))).isEmpty());
    }

    @Test
    void levelListenersHearEveryLevelChange() {
        List<String> changes = new ArrayList<>();
        progress.addLevelListener((uuid, job, before, after) -> changes.add(before + ">" + after));
        UUID id = player.getUniqueId();
        progress.addJobXp(id, mining, 1);
        progress.setJobLevel(id, mining, 4);
        progress.setJobLevel(id, mining, 4);
        progress.setJobLevel(id, mining, 2);
        progress.setJobXp(id, mining, 0);
        assertEquals(List.of("1>4", "4>2", "2>1"), changes);
    }

    @Test
    void syncChangesTheRecipeBookWithTheLevel() {
        progress.setJobLevel(player.getUniqueId(), mining, 2);
        book.sync(player);
        assertTrue(player.hasDiscoveredRecipe(book.key("test_gear")));
        progress.setJobLevel(player.getUniqueId(), mining, 1);
        book.sync(player);
        assertFalse(player.hasDiscoveredRecipe(book.key("test_gear")));
    }

    @Test
    void recipeNeedsKnownItemsAndValidShape() {
        assertThrows(
                IllegalArgumentException.class,
                () -> book.register(new CraftingRecipe(
                        "bad", mining, 1, "nope", 1, List.of("X"), Map.of('X', Ingredient.vanilla(Material.DIRT)))));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CraftingRecipe("bad2", mining, 1, "gear", 1, List.of("XY"), Map.of('X', Ingredient.vanilla(Material.DIRT))));
    }

    private static final class MemoryStorage implements PlayerStorage {
        private final Map<String, String> data = new HashMap<>();

        @Override
        public Optional<String> get(UUID player, String key) {
            return Optional.ofNullable(data.get(player + key));
        }

        @Override
        public void set(UUID player, String key, String value) {
            data.put(player + key, value);
        }

        @Override
        public void remove(UUID player, String key) {
            data.remove(player + key);
        }
    }
}
