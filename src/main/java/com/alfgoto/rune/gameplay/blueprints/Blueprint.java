package com.alfgoto.rune.gameplay.blueprints;

import com.alfgoto.rune.foundations.registry.Identified;
import java.util.Locale;
import java.util.Objects;

/**
 * A Blueprint: the id of a craftable Legendary recipe (a {@code CraftingRecipe} made with
 * {@code CraftingRecipe.blueprint}) that a player unlocks by using the Blueprint item. The Blueprint id is the
 * recipe id. Where a Blueprint comes from (milestone, chest, Quest) is decided elsewhere.
 */
public record Blueprint(String id) implements Identified {

    public Blueprint {
        Objects.requireNonNull(id, "id");
        if (id.isBlank() || !id.equals(id.toLowerCase(Locale.ROOT)) || id.contains(" ")) {
            throw new IllegalArgumentException("Blueprint id must be lower case with no spaces: '" + id + "'");
        }
    }

    /** Translation key of the name of what it unlocks, e.g. {@code rune.blueprint.briseroc}. */
    public String translationKey() {
        return "rune.blueprint." + id;
    }

    /** English fallback from the id: {@code heart_of_the_mountain} becomes {@code Heart Of The Mountain}. */
    public String fallbackName() {
        StringBuilder name = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }
}
