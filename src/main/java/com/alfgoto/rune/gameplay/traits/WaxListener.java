package com.alfgoto.rune.gameplay.traits;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jspecify.annotations.Nullable;

/**
 * Wax: a honeycomb and a Copper tool in the crafting grid (shapeless) give the same tool, waxed. The stage is then
 * locked for good, there is no way to remove the wax. The server recipe only accepts a wooden pickaxe or axe (the
 * base of Copper tools) with a honeycomb; this listener gives the result, and none when the tool is not an unwaxed
 * Copper tool. The tool is copied as it is, so its Runes and its count stay.
 */
final class WaxListener implements Listener {

    /** The recipe key path, in the plugin namespace. */
    static final String RECIPE_KEY = "wax_copper";

    private final NamespacedKey key;
    private final CopperOxidation copper;

    WaxListener(NamespacedKey key, CopperOxidation copper) {
        this.key = key;
        this.copper = copper;
    }

    /** The server recipe: 1 honeycomb + 1 wooden pickaxe or axe, in any place of the grid. */
    static ShapelessRecipe recipe(NamespacedKey key) {
        ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.WOODEN_PICKAXE));
        recipe.addIngredient(Material.HONEYCOMB);
        recipe.addIngredient(new RecipeChoice.MaterialChoice(Material.WOODEN_PICKAXE, Material.WOODEN_AXE));
        return recipe;
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        if (!isWax(event.getRecipe())) {
            return;
        }
        event.getInventory().setResult(waxed(event.getInventory()));
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!isWax(event.getRecipe())) {
            return;
        }
        ItemStack result = waxed(event.getInventory());
        if (result == null || (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)) {
            event.setCancelled(true);
            return;
        }
        event.setCurrentItem(result);
    }

    private boolean isWax(@Nullable Recipe recipe) {
        return recipe instanceof Keyed keyed && keyed.getKey().equals(key);
    }

    /** A waxed copy of the unwaxed Copper tool in the grid, or null when there is none. */
    private @Nullable ItemStack waxed(CraftingInventory inventory) {
        for (ItemStack stack : inventory.getMatrix()) {
            if (stack == null || stack.getType() == Material.HONEYCOMB) {
                continue;
            }
            if (copper.copperTool(stack).isEmpty() || copper.waxed(stack)) {
                return null;
            }
            ItemStack result = stack.clone();
            return copper.wax(result) ? result : null;
        }
        return null;
    }
}
