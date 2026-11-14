package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.Material;

/**
 * The base of every custom item (ADR 0002): an id, a Rarity, a vanilla material and a model.
 * Each item type adds what is specific to it later (slots, Rank…).
 *
 * <p>{@code model} is the name of an item definition of the resource pack, in the {@code rune}
 * namespace (e.g. {@code test_rune} for {@code assets/rune/items/test_rune.json}).
 */
public record RuneItem(String id, Rarity rarity, Material material, String model) implements Identified {

    public RuneItem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(rarity, "rarity");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(model, "model");
        if (id.isBlank() || !id.equals(id.toLowerCase(Locale.ROOT)) || id.contains(" ")) {
            throw new IllegalArgumentException("Item id must be lower case with no spaces: '" + id + "'");
        }
    }

    /** Translation key of the item name, e.g. {@code rune.item.test_item}. */
    public String translationKey() {
        return Text.key("item", id);
    }

    /** English fallback from the id: {@code test_item} becomes {@code Test Item}. */
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
