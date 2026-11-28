package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

/**
 * All {@link CraftingRecipe}s. Shared as a service; content modules fill it (ADR 0004).
 * Registering a recipe also adds it to the server. The Job Level decides who may craft it:
 * {@link #isUnlocked} is the source of truth, and the vanilla recipe book is kept in sync with it.
 */
public final class RecipeBook {

    private static final String KEY_PREFIX = "recipe/";
    private static final List<String> EQUIPMENT_SUFFIXES = List.of(
            "_PICKAXE", "_AXE", "_SHOVEL", "_HOE", "_SWORD", "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS");
    private static final Set<String> EQUIPMENT_MATERIALS = Set.of(
            "BOW", "CROSSBOW", "TRIDENT", "MACE", "SHIELD", "FISHING_ROD", "SHEARS", "TURTLE_HELMET");

    private final Plugin plugin;
    private final Server server;
    private final ItemRegistry items;
    private final JobProgress progress;
    private final Predicate<ItemStack> isBroken;
    private final Registry<CraftingRecipe> recipes = new Registry<>("recipes");
    private BiPredicate<UUID, String> blueprintKnown = (player, id) -> false;
    private final List<CraftingRecipe> pending = new ArrayList<>();
    private boolean deferred;

    public RecipeBook(Plugin plugin, ItemRegistry items, JobProgress progress) {
        this(plugin, items, progress, stack -> false);
    }

    /** {@code isBroken}: a Broken Rune item (0 durability) is refused as a Rune ingredient, it must be repaired first. */
    public RecipeBook(Plugin plugin, ItemRegistry items, JobProgress progress, Predicate<ItemStack> isBroken) {
        this.isBroken = isBroken;
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.items = items;
        this.progress = progress;
    }

    /**
     * Adds the recipe. It is registered on the server at once, unless {@link #deferRegistration} is on: then it waits
     * for {@link #flush}. Its result and Rune ingredients must exist.
     */
    public CraftingRecipe register(CraftingRecipe recipe) {
        recipes.register(recipe);
        if (deferred) {
            pending.add(recipe);
        } else {
            server.addRecipe(shaped(recipe));
        }
        return recipe;
    }

    /**
     * Holds back server registration until {@link #flush}. The result stack is built at flush time, when every item
     * redrawer (name, lore) is registered, so the vanilla recipe book shows the Rune name.
     */
    public void deferRegistration() {
        deferred = true;
    }

    /** Adds every held recipe to the server, then registers later ones at once. Refreshes online players' books. */
    public void flush() {
        deferred = false;
        for (CraftingRecipe recipe : pending) {
            server.addRecipe(shaped(recipe));
        }
        pending.clear();
        server.getOnlinePlayers().forEach(this::sync);
    }

    private ShapedRecipe shaped(CraftingRecipe recipe) {
        ShapedRecipe shaped = new ShapedRecipe(key(recipe.id()), items.create(items.get(recipe.result()), recipe.amount()));
        shaped.setCategory(category(items.get(recipe.result()).material()));
        shaped.shape(recipe.shape().toArray(String[]::new));
        recipe.ingredients().forEach((symbol, ingredient) -> shaped.setIngredient(symbol, choice(ingredient)));
        return shaped;
    }

    /** Vanilla recipe book tab: tools, weapons and armor go to Tools, everything else (Components, Blueprints) to Misc. */
    static CraftingBookCategory category(Material material) {
        String name = material.name();
        boolean gear = EQUIPMENT_SUFFIXES.stream().anyMatch(name::endsWith) || EQUIPMENT_MATERIALS.contains(name);
        return gear ? CraftingBookCategory.EQUIPMENT : CraftingBookCategory.MISC;
    }

    private RecipeChoice choice(Ingredient ingredient) {
        if (ingredient.isRune()) {
            // Base material only. An exact stack would also compare durability, Runes, Mastery, name and lore,
            // so a used or upgraded tool would never match. match() checks the Rune id of each cell instead.
            return new RecipeChoice.MaterialChoice(items.get(ingredient.itemId()).material());
        }
        return new RecipeChoice.MaterialChoice(ingredient.material());
    }

    public Collection<CraftingRecipe> all() {
        return recipes.all();
    }

    public Optional<CraftingRecipe> find(String id) {
        return recipes.find(id);
    }

    /** The Rune recipe this server recipe is, or empty for a vanilla recipe. */
    public Optional<CraftingRecipe> of(Recipe recipe) {
        if (recipe instanceof Keyed keyed
                && keyed.getKey().getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT))
                && keyed.getKey().getKey().startsWith(KEY_PREFIX)) {
            return recipes.find(keyed.getKey().getKey().substring(KEY_PREFIX.length()));
        }
        return Optional.empty();
    }

    /**
     * The Rune recipe this crafting grid really makes, checking each cell: a Rune ingredient needs that Rune item
     * (any durability but not Broken, any Runes or Mastery), a vanilla ingredient needs that material and not a Rune item. Like vanilla,
     * the pattern can sit anywhere in the grid and can be mirrored. Empty when no Rune recipe matches.
     *
     * <p>Many Rune items share a base material (every Rune pickaxe is a wooden pickaxe, every Component is paper),
     * so the server may pick the wrong Rune recipe or none: this is the source of truth.
     */
    public Optional<CraftingRecipe> match(ItemStack[] matrix) {
        List<List<ItemStack>> grid = trim(matrix);
        for (CraftingRecipe recipe : recipes.all()) {
            List<String> pattern = trim(recipe.shape());
            if (fits(recipe, pattern, grid, false) || fits(recipe, pattern, grid, true)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private boolean fits(CraftingRecipe recipe, List<String> pattern, List<List<ItemStack>> grid, boolean mirrored) {
        if (pattern.size() != grid.size() || pattern.get(0).length() != grid.get(0).size()) {
            return false;
        }
        int width = pattern.get(0).length();
        for (int row = 0; row < pattern.size(); row++) {
            for (int col = 0; col < width; col++) {
                char symbol = pattern.get(row).charAt(mirrored ? width - 1 - col : col);
                if (!cellFits(symbol == ' ' ? null : recipe.ingredients().get(symbol), grid.get(row).get(col))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean cellFits(Ingredient ingredient, ItemStack stack) {
        if (ingredient == null) {
            return isEmpty(stack);
        }
        if (isEmpty(stack)) {
            return false;
        }
        if (ingredient.isRune()) {
            return items.of(stack).map(item -> item.id().equals(ingredient.itemId())).orElse(false)
                    && !isBroken.test(stack);
        }
        return stack.getType() == ingredient.material() && items.of(stack).isEmpty();
    }

    /** What a recipe book click puts in the grid ({@code side} by {@code side}, row by row) and what is left of the storage. */
    public record Fill(ItemStack[] grid, ItemStack[] storage) {}

    /**
     * Fills a crafting grid for a recipe book click, in the recipe's shape from the top-left corner. Each cell takes
     * a stack that passes {@link #cellFits}, so a Rune ingredient only takes that Rune item and a vanilla one never
     * takes a Rune item. {@code makeAll} puts as many crafts as the storage allows (one more per cell, up to the
     * stack size), otherwise one craft. The given storage is not changed: the new storage comes back in the result.
     * Empty when the recipe does not fit the grid or the storage misses an ingredient.
     */
    public Optional<Fill> fill(CraftingRecipe recipe, ItemStack[] storage, int side, boolean makeAll) {
        List<String> pattern = trim(recipe.shape());
        if (pattern.size() > side || pattern.get(0).length() > side) {
            return Optional.empty();
        }
        Fill best = null;
        for (int crafts = 1; crafts <= 64; crafts++) {
            Fill attempt = fillCrafts(recipe, pattern, storage, side, crafts);
            if (attempt == null) {
                break;
            }
            best = attempt;
            if (!makeAll) {
                break;
            }
        }
        return Optional.ofNullable(best);
    }

    private Fill fillCrafts(CraftingRecipe recipe, List<String> pattern, ItemStack[] storage, int side, int crafts) {
        ItemStack[] left = new ItemStack[storage.length];
        for (int i = 0; i < storage.length; i++) {
            left[i] = isEmpty(storage[i]) ? null : storage[i].clone();
        }
        ItemStack[] grid = new ItemStack[side * side];
        for (int row = 0; row < pattern.size(); row++) {
            for (int col = 0; col < pattern.get(row).length(); col++) {
                char symbol = pattern.get(row).charAt(col);
                if (symbol == ' ') {
                    continue;
                }
                ItemStack placed = take(recipe.ingredients().get(symbol), left, crafts);
                if (placed == null) {
                    return null;
                }
                grid[row * side + col] = placed;
            }
        }
        return new Fill(grid, left);
    }

    /** {@code amount} identical items for one cell, taken out of {@code left}; null when they are not all there. */
    private ItemStack take(Ingredient ingredient, ItemStack[] left, int amount) {
        for (int first = 0; first < left.length; first++) {
            ItemStack model = left[first];
            if (model == null || !cellFits(ingredient, model)) {
                continue;
            }
            if (amount > model.getMaxStackSize()) {
                return null;
            }
            int available = 0;
            for (ItemStack stack : left) {
                if (stack != null && stack.isSimilar(model)) {
                    available += stack.getAmount();
                }
            }
            if (available < amount) {
                continue; // not enough of this kind, maybe another fitting kind has enough
            }
            ItemStack placed = model.clone();
            placed.setAmount(amount);
            int need = amount;
            for (int slot = 0; slot < left.length && need > 0; slot++) {
                ItemStack stack = left[slot];
                if (stack == null || !stack.isSimilar(placed)) {
                    continue;
                }
                int moved = Math.min(need, stack.getAmount());
                need -= moved;
                stack.setAmount(stack.getAmount() - moved);
                if (stack.getAmount() <= 0) {
                    left[slot] = null;
                }
            }
            return placed;
        }
        return null;
    }

    /** The grid cut down to the smallest rectangle holding every item. Empty grid gives a 0 by 0 list. */
    private static List<List<ItemStack>> trim(ItemStack[] matrix) {
        int side = (int) Math.round(Math.sqrt(matrix.length));
        int top = side, bottom = -1, left = side, right = -1;
        for (int i = 0; i < matrix.length; i++) {
            if (!isEmpty(matrix[i])) {
                top = Math.min(top, i / side);
                bottom = Math.max(bottom, i / side);
                left = Math.min(left, i % side);
                right = Math.max(right, i % side);
            }
        }
        List<List<ItemStack>> grid = new ArrayList<>();
        for (int row = top; row <= bottom; row++) {
            List<ItemStack> cells = new ArrayList<>();
            for (int col = left; col <= right; col++) {
                cells.add(matrix[row * side + col]);
            }
            grid.add(cells);
        }
        return grid.isEmpty() ? List.of(List.of()) : grid;
    }

    /** The shape padded to a rectangle, then cut down to the smallest rectangle holding every symbol. */
    private static List<String> trim(List<String> shape) {
        int width = shape.stream().mapToInt(String::length).max().orElse(0);
        int top = shape.size(), bottom = -1, left = width, right = -1;
        for (int row = 0; row < shape.size(); row++) {
            String line = shape.get(row);
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) != ' ') {
                    top = Math.min(top, row);
                    bottom = Math.max(bottom, row);
                    left = Math.min(left, col);
                    right = Math.max(right, col);
                }
            }
        }
        List<String> pattern = new ArrayList<>();
        for (int row = top; row <= bottom; row++) {
            String line = String.format("%-" + width + "s", shape.get(row));
            pattern.add(line.substring(left, right + 1));
        }
        return pattern.isEmpty() ? List.of("") : pattern;
    }

    private static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir() || stack.getAmount() <= 0;
    }

    /** Sets how a Blueprint recipe is unlocked: {@code known(player, blueprint id)}. The default knows none. */
    public void setBlueprintCheck(BiPredicate<UUID, String> known) {
        this.blueprintKnown = known;
    }

    /** A Blueprint recipe needs its Blueprint used; any other recipe needs its Job Level. */
    public boolean isUnlocked(UUID player, CraftingRecipe recipe) {
        if (recipe.blueprint()) {
            return blueprintKnown.test(player, recipe.id());
        }
        return progress.jobLevel(player, recipe.job()) >= recipe.level();
    }

    /** Makes the player's recipe book match their Job Levels: unlocked recipes shown, others removed. */
    public void sync(Player player) {
        for (CraftingRecipe recipe : recipes.all()) {
            NamespacedKey key = key(recipe.id());
            if (isUnlocked(player.getUniqueId(), recipe)) {
                player.discoverRecipe(key);
            } else {
                player.undiscoverRecipe(key);
            }
        }
    }

    /** Removes every Rune recipe from the server. */
    public void unregisterAll() {
        for (CraftingRecipe recipe : recipes.all()) {
            server.removeRecipe(key(recipe.id()));
        }
    }

    public NamespacedKey key(String id) {
        return new NamespacedKey(plugin, KEY_PREFIX + id);
    }
}
