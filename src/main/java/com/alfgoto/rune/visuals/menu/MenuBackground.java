package com.alfgoto.rune.visuals.menu;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * A menu's v3 background (docs/art-direction.md, "Menus"). It is a glyph of the {@code rune:menus} font put in
 * front of the menu title: shifted 8 px left so it covers the whole chest window, then the title text is
 * shifted back to its usual place. Textures come from {@code tools/texture-gen/v3/menus.py}.
 */
public enum MenuBackground {
    CODEX_MAIN('', 3),
    JOBS('', 4),
    QUESTS('', 3),
    RUNES('', 6);

    public static final Key FONT = Key.key("rune", "menus");
    /** Space glyphs of the font: -8 px before the background, -169 px after it (176 px wide + 1 - 8). */
    static final char SHIFT_LEFT = '';
    static final char SHIFT_BACK = '';

    private final char glyph;
    private final int rows;

    MenuBackground(char glyph, int rows) {
        this.glyph = glyph;
        this.rows = rows;
    }

    /** The menu height this background is drawn for. */
    public int rows() {
        return rows;
    }

    /**
     * The background followed by {@code title}. The glyph is white: a bitmap glyph is multiplied by the
     * text colour, and menu titles are dark grey by default.
     */
    public Component title(Component title) {
        return Component.text()
                .append(Component.text("" + SHIFT_LEFT + glyph + SHIFT_BACK, NamedTextColor.WHITE).font(FONT))
                .append(title)
                .build();
    }
}
