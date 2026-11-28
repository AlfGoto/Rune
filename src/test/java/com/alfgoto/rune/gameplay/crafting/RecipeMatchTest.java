package com.alfgoto.rune.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/**
 * Rune ingredients are matched by Rune id, not by exact stack: a used tool, with Runes and a custom name,
 * still crafts. Recipes that share base materials (every Rune pickaxe is a wooden pickaxe, every Component is
 * paper) each make their own result.
 */
class RecipeMatchTest {

    private final Job mining = Job.builder("mining").build();
    private ServerMock server;
    private RunePlugin plugin;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private RecipeBook book;
    private Rune speed;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        JobProgress progress = new JobProgress(new Memory(), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
        Effect<?> effect = Effect.builder("speed", EffectTriggers.BLOCK_BREAK).does(context -> {});
        speed = runes.register(new Rune(
                new RuneItem("speed", Rarity.COMMON, Material.PAPER, "test_rune"), effect.with(10, 10), Set.of(Fit.PICKAXE)));
        items.register(new RuneItem("lens", Rarity.COMMON, Material.PAPER, "test_rune"));
        items.register(new RuneItem("core", Rarity.COMMON, Material.PAPER, "test_rune"));
        items.register(RuneItem.tool("stone_pick", Rarity.COMMON, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        items.register(RuneItem.tool("lens_pick", Rarity.RARE, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        items.register(RuneItem.tool("core_pick", Rarity.RARE, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        book = new RecipeBook(plugin, items, progress, new BrokenToolListener(items)::isBroken);
        // Same shape, same base materials (paper over wooden pickaxe): only the Rune ids differ.
        book.register(new CraftingRecipe("lens_pick", mining, 1, "lens_pick", 1, List.of("L", "P"),
                Map.of('L', Ingredient.rune("lens"), 'P', Ingredient.rune("stone_pick"))));
        book.register(new CraftingRecipe("core_pick", mining, 1, "core_pick", 1, List.of("C", "P"),
                Map.of('C', Ingredient.rune("core"), 'P', Ingredient.rune("stone_pick"))));
        book.register(new CraftingRecipe("mirror", mining, 1, "core", 1, List.of("I ", "IL"),
                Map.of('I', Ingredient.vanilla(Material.IRON_INGOT), 'L', Ingredient.rune("lens"))));
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack item(String id) {
        return items.create(items.get(id), 1);
    }

    /** A pickaxe as a player really has it: worn, renamed, with a Rune installed. */
    private ItemStack usedPick() {
        ItemStack pick = item("stone_pick");
        loadout.install(pick, speed, 1);
        pick.editMeta(meta -> {
            ((Damageable) meta).setDamage(17);
            meta.setDisplayName("My pick");
        });
        return pick;
    }

    private static ItemStack[] grid(ItemStack... cells) {
        ItemStack[] matrix = new ItemStack[9];
        System.arraycopy(cells, 0, matrix, 0, cells.length);
        return matrix;
    }

    @Test
    void aUsedToolWithRunesStillMatches() {
        assertEquals("lens_pick", book.match(grid(null, item("lens"), null, null, usedPick())).orElseThrow().id());
    }

    @Test
    void aBrokenToolIsRefusedUntilRepaired() {
        ItemStack pick = usedPick();
        pick.editMeta(meta -> ((Damageable) meta).setDamage(((Damageable) meta).getMaxDamage()));
        assertTrue(book.match(grid(item("lens"), null, null, pick)).isEmpty());
    }

    @Test
    void recipesSharingBaseMaterialsEachMakeTheirOwnResult() {
        assertEquals("lens_pick", book.match(grid(item("lens"), null, null, usedPick())).orElseThrow().id());
        assertEquals("core_pick", book.match(grid(item("core"), null, null, usedPick())).orElseThrow().id());
    }

    @Test
    void theWrongRuneItemOrAVanillaStackDoesNotMatch() {
        assertTrue(book.match(grid(item("speed"), null, null, usedPick())).isEmpty());
        assertTrue(book.match(grid(item("lens"), null, null, new ItemStack(Material.WOODEN_PICKAXE))).isEmpty());
    }

    @Test
    void aVanillaIngredientRefusesARuneItemOfTheSameMaterial() {
        items.register(new RuneItem("fake_ingot", Rarity.COMMON, Material.IRON_INGOT, "test_rune"));
        ItemStack fake = item("fake_ingot");
        assertTrue(book.match(grid(fake, null, null, fake, item("lens"))).isEmpty());
    }

    @Test
    void theShapeCanBeMovedAndMirrored() {
        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        assertEquals("mirror", book.match(grid(null, iron, null, null, iron, item("lens"))).orElseThrow().id());
        assertEquals("mirror", book.match(grid(null, null, iron, null, item("lens"), iron)).orElseThrow().id());
        assertTrue(book.match(grid(iron, null, null, item("lens"), iron)).isEmpty()); // upside down
    }

    @Test
    void the2By2PlayerGridWorks() {
        assertEquals("core_pick", book.match(new ItemStack[] {null, item("core"), null, usedPick()}).orElseThrow().id());
    }

    @Test
    void preparedResultFollowsTheRealRecipeEvenWhenTheServerPickedTheOther() {
        CraftingInventory inventory = prepare(grid(item("core"), null, null, usedPick()), "lens_pick");
        assertEquals("core_pick", items.of(inventory.getResult()).orElseThrow().id());
    }

    @Test
    void preparedResultIsRemovedWhenTheRuneItemsAreWrong() {
        assertNull(prepare(grid(item("speed"), null, null, usedPick()), "lens_pick").getResult());
    }

    private ItemStack[] storage(ItemStack... stacks) {
        ItemStack[] storage = new ItemStack[36];
        System.arraycopy(stacks, 0, storage, 0, stacks.length);
        return storage;
    }

    @Test
    void fillTakesTheRightRuneItemEvenWhenAnotherComesFirst() {
        CraftingRecipe recipe = book.find("lens_pick").orElseThrow();
        ItemStack[] storage = storage(item("core"), usedPick(), item("lens"));
        RecipeBook.Fill fill = book.fill(recipe, storage, 3, false).orElseThrow();
        assertEquals("lens", items.of(fill.grid()[0]).orElseThrow().id());
        assertEquals("stone_pick", items.of(fill.grid()[3]).orElseThrow().id());
        assertEquals("core", items.of(fill.storage()[0]).orElseThrow().id()); // the other Rune item stays
        assertNull(fill.storage()[2]);
        assertEquals("lens_pick", book.match(fill.grid()).orElseThrow().id());
    }

    @Test
    void fillRefusesWhenAnIngredientIsMissingOrBroken() {
        CraftingRecipe recipe = book.find("lens_pick").orElseThrow();
        assertTrue(book.fill(recipe, storage(item("lens"), item("core")), 3, false).isEmpty());
        ItemStack broken = usedPick();
        broken.editMeta(meta -> ((Damageable) meta).setDamage(((Damageable) meta).getMaxDamage()));
        assertTrue(book.fill(recipe, storage(item("lens"), broken), 3, false).isEmpty());
    }

    @Test
    void fillGivesAVanillaIngredientNeverARuneItem() {
        items.register(new RuneItem("fake_ingot", Rarity.COMMON, Material.IRON_INGOT, "test_rune"));
        CraftingRecipe recipe = book.find("mirror").orElseThrow();
        ItemStack[] storage = storage(item("fake_ingot"), new ItemStack(Material.IRON_INGOT, 2), item("lens"));
        RecipeBook.Fill fill = book.fill(recipe, storage, 3, false).orElseThrow();
        assertEquals(Material.IRON_INGOT, fill.grid()[0].getType());
        assertTrue(items.of(fill.grid()[0]).isEmpty());
        assertEquals(1, fill.storage()[1].getAmount());
    }

    @Test
    void fillMakeAllPutsAsManyCraftsAsTheStorageAllows() {
        CraftingRecipe recipe = book.find("mirror").orElseThrow();
        ItemStack lenses = item("lens");
        lenses.setAmount(3);
        ItemStack[] storage = storage(new ItemStack(Material.IRON_INGOT, 20), lenses);
        RecipeBook.Fill fill = book.fill(recipe, storage, 3, true).orElseThrow();
        assertEquals(3, fill.grid()[0].getAmount());
        assertEquals(3, fill.grid()[4].getAmount());
        assertEquals(14, fill.storage()[0].getAmount());
        assertEquals(1, book.fill(recipe, storage, 3, false).orElseThrow().grid()[0].getAmount());
    }

    @Test
    void fillRefusesARecipeTooBigForTheGrid() {
        book.register(new CraftingRecipe("big", mining, 1, "core", 1, List.of("III", "III", "III"),
                Map.of('I', Ingredient.vanilla(Material.IRON_INGOT))));
        ItemStack[] storage = storage(new ItemStack(Material.IRON_INGOT, 9));
        assertTrue(book.fill(book.find("big").orElseThrow(), storage, 2, false).isEmpty());
        assertTrue(book.fill(book.find("big").orElseThrow(), storage, 3, false).isPresent());
    }

    @Test
    void aRecipeBookClickFillsTheGridWithTheRightRuneItem() {
        InventoryView view = player.openWorkbench(null, true);
        CraftingInventory grid = (CraftingInventory) view.getTopInventory();
        // Vanilla filled the grid with the wrong Rune item (same base material).
        grid.setMatrix(grid(item("core"), null, null, usedPick()));
        player.getInventory().setItem(0, item("lens"));
        var click = new com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent(
                player, book.key("lens_pick"), false);
        new RecipeBookClickListener(server, book).onClick(click);
        assertTrue(click.isCancelled());
        assertEquals("lens", items.of(grid.getMatrix()[0]).orElseThrow().id());
        assertEquals("stone_pick", items.of(grid.getMatrix()[3]).orElseThrow().id());
        assertTrue(java.util.Arrays.stream(player.getInventory().getStorageContents())
                .anyMatch(stack -> stack != null && items.of(stack).map(i -> i.id().equals("core")).orElse(false)));
    }

    @Test
    void aVanillaRecipeClickIsLeftToVanilla() {
        player.openWorkbench(null, true);
        var click = new com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent(
                player, org.bukkit.NamespacedKey.minecraft("chest"), false);
        new RecipeBookClickListener(server, book).onClick(click);
        assertFalse(click.isCancelled());
    }

    private CraftingInventory prepare(ItemStack[] matrix, String serverPick) {
        InventoryView view = player.openWorkbench(null, true);
        CraftingInventory inventory = (CraftingInventory) view.getTopInventory();
        inventory.setMatrix(matrix);
        inventory.setResult(item(serverPick));
        new CraftingListener(book, items, loadout, runes).onPrepare(new PrepareItemCraftEvent(inventory, view, false) {
            @Override
            public Recipe getRecipe() {
                return server.getRecipe(book.key(serverPick));
            }
        });
        return inventory;
    }

    private static final class Memory implements PlayerStorage {
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
