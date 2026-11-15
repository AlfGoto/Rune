package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
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
    private final Registry<CraftingRecipe> recipes = new Registry<>("recipes");

    public RecipeBook(Plugin plugin, ItemRegistry items, JobProgress progress) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.items = items;
        this.progress = progress;
    }

    /** Adds the recipe and registers it on the server. Its result and Rune ingredients must exist. */
    public CraftingRecipe register(CraftingRecipe recipe) {
        ShapedRecipe shaped = new ShapedRecipe(key(recipe.id()), items.create(items.get(recipe.result()), recipe.amount()));
        shaped.shape(recipe.shape().toArray(String[]::new));
        recipe.ingredients().forEach((symbol, ingredient) -> shaped.setIngredient(symbol, choice(ingredient)));
        recipes.register(recipe);
        server.addRecipe(shaped);
        return recipe;
    }

    private RecipeChoice choice(Ingredient ingredient) {
        if (ingredient.isRune()) {
            return new RecipeChoice.ExactChoice(items.create(items.get(ingredient.itemId()), 1));
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

    public boolean isUnlocked(UUID player, CraftingRecipe recipe) {
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
