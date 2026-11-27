package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        shaped.shape(recipe.shape().toArray(String[]::new));
        recipe.ingredients().forEach((symbol, ingredient) -> shaped.setIngredient(symbol, choice(ingredient)));
        return shaped;
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
