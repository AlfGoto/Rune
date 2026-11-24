package com.alfgoto.rune.gameplay.blueprints;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.storage.PlayerStorage;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * Blueprints: one item ({@link #ITEM_ID}) that holds a Blueprint id, and the list of Blueprints each player has
 * used. Using a Blueprint unlocks its recipe for good (stored in {@link PlayerStorage}, key
 * {@code blueprint/<id>}); the recipe book is synced at once. Shared as a service; content registers Blueprints.
 */
public final class Blueprints {

    /** The item every Blueprint is made from. The Blueprint id is in the stack's data. */
    public static final String ITEM_ID = "blueprint";

    private static final String KEY_PREFIX = "blueprint/";

    private final ItemRegistry items;
    private final RecipeBook book;
    private final PlayerStorage storage;
    private final NamespacedKey idKey;
    private final Registry<Blueprint> blueprints = new Registry<>("blueprints");

    public Blueprints(Plugin plugin, ItemRegistry items, RecipeBook book, PlayerStorage storage) {
        this.items = items;
        this.book = book;
        this.storage = storage;
        this.idKey = new NamespacedKey(plugin, "blueprint_id");
    }

    /** Registers a Blueprint. Its recipe must already be in the RecipeBook and be a Blueprint recipe. */
    public Blueprint register(Blueprint blueprint) {
        CraftingRecipe recipe = book.find(blueprint.id()).orElse(null);
        if (recipe == null || !recipe.blueprint()) {
            throw new IllegalArgumentException(
                    "Blueprint '" + blueprint.id() + "' needs a Blueprint recipe with the same id");
        }
        return blueprints.register(blueprint);
    }

    public Optional<Blueprint> find(String id) {
        return blueprints.find(id);
    }

    public Collection<Blueprint> all() {
        return blueprints.all();
    }

    /** A new Blueprint item. */
    public ItemStack create(Blueprint blueprint) {
        ItemStack stack = items.create(items.get(ITEM_ID), 1);
        items.update(
                stack, meta -> meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, blueprint.id()));
        return stack;
    }

    /** The Blueprint this stack is, or empty when it is not a Blueprint item (or its Blueprint is unknown). */
    public Optional<Blueprint> of(@Nullable ItemStack stack) {
        return idOf(stack).flatMap(blueprints::find);
    }

    /** The Blueprint id written in a Blueprint stack, even when no such Blueprint is registered. For drawing. */
    public Optional<String> idOf(@Nullable ItemStack stack) {
        if (items.of(stack).filter(item -> item.id().equals(ITEM_ID)).isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING));
    }

    /** True when the player has used this Blueprint. */
    public boolean isUnlocked(UUID player, String blueprintId) {
        return storage.get(player, KEY_PREFIX + blueprintId).isPresent();
    }

    /** Unlocks the recipe for good and syncs the player's recipe book. False when it was already unlocked. */
    public boolean unlock(Player player, Blueprint blueprint) {
        if (isUnlocked(player.getUniqueId(), blueprint.id())) {
            return false;
        }
        storage.set(player.getUniqueId(), KEY_PREFIX + blueprint.id(), "1");
        book.sync(player);
        return true;
    }
}
