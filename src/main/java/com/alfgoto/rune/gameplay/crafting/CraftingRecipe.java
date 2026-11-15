package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.gameplay.jobs.Job;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A shaped recipe that makes a Rune item and is unlocked by a Job Level.
 *
 * <p>{@code shape} is 1 to 3 rows of 1 to 3 characters, a space is an empty cell. Every other
 * character must be in {@code ingredients}.
 */
public record CraftingRecipe(
        String id,
        Job job,
        int level,
        String result,
        int amount,
        List<String> shape,
        Map<Character, Ingredient> ingredients)
        implements Identified {

    public CraftingRecipe {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(job, "job");
        Objects.requireNonNull(result, "result");
        if (level < 1) {
            throw new IllegalArgumentException("Recipe level must be 1 or more, got " + level);
        }
        if (amount < 1) {
            throw new IllegalArgumentException("Recipe amount must be 1 or more, got " + amount);
        }
        shape = List.copyOf(shape);
        ingredients = Map.copyOf(ingredients);
        if (shape.isEmpty() || shape.size() > 3) {
            throw new IllegalArgumentException("Recipe '" + id + "' needs 1 to 3 rows");
        }
        for (String row : shape) {
            if (row.isEmpty() || row.length() > 3) {
                throw new IllegalArgumentException("Recipe '" + id + "' rows need 1 to 3 characters");
            }
            for (char c : row.toCharArray()) {
                if (c != ' ' && !ingredients.containsKey(c)) {
                    throw new IllegalArgumentException("Recipe '" + id + "' has no ingredient for '" + c + "'");
                }
            }
        }
    }
}
