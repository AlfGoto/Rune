package com.alfgoto.rune.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;

/**
 * Helpers for translatable components. Every component carries its English text as fallback,
 * so a missing key never shows a raw key.
 */
public final class Text {

    public static final String NAMESPACE = "rune";

    private Text() {}

    /** Builds {@code rune.<category>.<id>}. */
    public static String key(String category, String id) {
        return NAMESPACE + "." + category + "." + id;
    }

    /** A translatable component with English fallback. */
    public static TranslatableComponent translatable(String key, String englishFallback, ComponentLike... args) {
        return Component.translatable(key, englishFallback, args);
    }

    /** The Rarity name, coloured with its Rarity colour. */
    public static TranslatableComponent rarityName(Rarity rarity) {
        return translatable(rarity.translationKey(), rarity.englishName()).color(rarity.color());
    }
}
