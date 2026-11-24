package com.alfgoto.rune.content.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.crafting.CraftingListener;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import org.bukkit.Material;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/**
 * Issue #214: with every module loaded, stacks made by {@code ItemRegistry.create} AFTER loading (like
 * {@code /rune give}, with name and lore) craft the Copper Pickaxe and the next recipe in the line.
 */
class FreshIngredientsCraftTest {

    private ServerMock server;
    private ItemRegistry items;
    private RecipeBook book;
    private JobProgress progress;
    private JobRegistry jobs;
    private CraftingListener listener;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        book = plugin.context().get(RecipeBook.class);
        progress = plugin.context().get(JobProgress.class);
        jobs = plugin.context().get(JobRegistry.class);
        listener = new CraftingListener(
                book, items, plugin.context().get(ToolLoadout.class), plugin.context().get(RuneRegistry.class));
        player = server.addPlayer();
        progress.setJobLevel(player.getUniqueId(), jobs.registry().get("mining"), 11);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack fresh(String id) {
        return items.create(items.get(id), 1);
    }

    private ItemStack[] copperGrid(ItemStack pickaxe) {
        ItemStack copper = new ItemStack(Material.COPPER_INGOT);
        return new ItemStack[] {copper, copper.clone(), copper.clone(), fresh("reinforced_handle"), pickaxe, null};
    }

    private CraftingInventory prepare(ItemStack[] matrix, String serverPick) {
        InventoryView view = player.openWorkbench(null, true);
        CraftingInventory inventory = (CraftingInventory) view.getTopInventory();
        inventory.setMatrix(matrix);
        inventory.setResult(serverPick == null ? null : fresh(serverPick));
        listener.onPrepare(new PrepareItemCraftEvent(inventory, view, false) {
            @Override
            public Recipe getRecipe() {
                return serverPick == null ? null : server.getRecipe(book.key(serverPick));
            }
        });
        return inventory;
    }

    @Test
    void copperPickaxeCraftsFromFreshItems() {
        assertEquals("copper_pickaxe", book.match(copperGrid(fresh("coal_pickaxe"))).orElseThrow().id());
        CraftingInventory inventory = prepare(copperGrid(fresh("coal_pickaxe")), "copper_pickaxe");
        assertEquals("copper_pickaxe", items.of(inventory.getResult()).orElseThrow().id());
    }

    @Test
    void redstonePickaxeCraftsFromFreshItems() {
        ItemStack redstone = new ItemStack(Material.REDSTONE_BLOCK);
        ItemStack[] grid = {redstone, new ItemStack(Material.DIAMOND), redstone.clone(),
            fresh("gem_setting"), fresh("copper_pickaxe"), null};
        CraftingInventory inventory = prepare(grid, "redstone_pickaxe");
        assertEquals("redstone_pickaxe", items.of(inventory.getResult()).orElseThrow().id());
    }

    @Test
    void aVanillaToolOrAnotherRuneItemGivesNoResult() {
        assertNull(prepare(copperGrid(new ItemStack(Material.WOODEN_PICKAXE)), "copper_pickaxe").getResult());
        assertNull(prepare(copperGrid(fresh("flint_pickaxe")), "copper_pickaxe").getResult());
    }

    @Test
    void theJobLevelLockStillWorks() {
        progress.setJobLevel(player.getUniqueId(), jobs.registry().get("mining"), 1);
        assertNull(prepare(copperGrid(fresh("coal_pickaxe")), "copper_pickaxe").getResult());
    }
}
