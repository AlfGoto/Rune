package com.alfgoto.rune.gameplay.crafting;

import java.util.Objects;
import org.bukkit.Material;

/**
 * One ingredient of a recipe: a vanilla material, or a Rune item by id (a Component, a previous
 * gear piece…). Exactly one of the two is set.
 */
public record Ingredient(Material material, String itemId) {

    public Ingredient {
        if ((material == null) == (itemId == null)) {
            throw new IllegalArgumentException("An ingredient is a material or a Rune item id, not both or none");
        }
    }

    public static Ingredient vanilla(Material material) {
        return new Ingredient(Objects.requireNonNull(material, "material"), null);
    }

    public static Ingredient rune(String itemId) {
        return new Ingredient(null, Objects.requireNonNull(itemId, "itemId"));
    }

    public boolean isRune() {
        return itemId != null;
    }
}
