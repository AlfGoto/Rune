package com.alfgoto.rune.visuals.menu;

/**
 * A menu's v3 background (docs/art-direction.md, "Menus"): one 176x222 glyph of the {@code rune:menus} font,
 * drawn over the whole 6-row chest window by {@link MenuCanvas}. Textures and slot layouts come from
 * {@code tools/texture-gen/v3/menus.py}.
 */
public enum MenuBackground {
    CODEX('\ue100'),
    RUNES('\ue101'),
    QUESTS('\ue102'),
    JOBS('\ue103');

    /** Every background is a full 6-row chest window. */
    public static final int ROWS = 6;

    /** Width of the window texture; a bitmap glyph advances its width + 1. */
    static final int WIDTH = 176;

    private final char glyph;

    MenuBackground(char glyph) {
        this.glyph = glyph;
    }

    char glyph() {
        return glyph;
    }
}
