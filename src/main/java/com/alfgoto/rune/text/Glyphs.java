package com.alfgoto.rune.text;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/**
 * Glyph placeholders. A glyph is one character drawn with the {@code rune:glyphs} font.
 * The resource pack will provide the font; until then the character shows as a placeholder.
 */
public final class Glyphs {

    public static final Key FONT = Key.key("rune", "glyphs");

    private Glyphs() {}

    public static Component glyph(char character) {
        return Component.text(character).font(FONT);
    }
}
