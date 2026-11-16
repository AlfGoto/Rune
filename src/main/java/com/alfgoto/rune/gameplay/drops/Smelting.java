package com.alfgoto.rune.gameplay.drops;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/** What the vanilla furnace makes of an item: cobblestone → stone, log → charcoal. Cached per material. */
public final class Smelting {

    private final Map<Material, Material> cache = new HashMap<>();

    /** The smelted item, or {@code item} itself when no furnace recipe takes it. */
    public Material smelt(Material item) {
        return cache.computeIfAbsent(item, this::find);
    }

    private Material find(Material item) {
        ItemStack probe = new ItemStack(item);
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            if (recipes.next() instanceof FurnaceRecipe furnace
                    && furnace.getInputChoice().test(probe)
                    && !furnace.getResult().getType().isAir()) {
                return furnace.getResult().getType();
            }
        }
        return item;
    }
}
