package com.alfgoto.rune.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Icons drawn with the {@code rune:glyphs} font (one character each, private-use range). The pack's
 * {@code assets/rune/font/glyphs.json} maps them; textures come from {@code tools/texture-gen/v3/icons.py}.
 */
public final class Glyphs {

    public static final Key FONT = Key.key("rune", "glyphs");

    private Glyphs() {}

    /** First Rarity icon ({@code \ue000} = Common); the others follow in {@link Rarity} order. */
    static final char FIRST_RARITY = '\ue000';

    public static Component glyph(char character) {
        return Component.text(character).font(FONT);
    }

    /**
     * The Rarity's gem icon. White, so the text colour does not tint the icon's own colours
     * (bitmap glyphs are multiplied by the text colour).
     */
    public static Component rarity(Rarity rarity) {
        return glyph((char) (FIRST_RARITY + rarity.ordinal())).color(NamedTextColor.WHITE);
    }
}
