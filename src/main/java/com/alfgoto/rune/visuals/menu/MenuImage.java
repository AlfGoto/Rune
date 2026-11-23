package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.text.Rarity;

/**
 * A live image of the {@code rune:menus} font, drawn by {@link MenuCanvas#image}: its height is fixed in
 * the font, its x is chosen when drawing. Textures come from {@code tools/texture-gen/v3/menus.py}.
 */
public enum MenuImage {
    NOTE_COMMON('', 52),
    NOTE_UNCOMMON('', 52),
    NOTE_RARE('', 52),
    NOTE_EPIC('', 52),
    NOTE_LEGENDARY('', 52),
    ORB_0('', 31),
    ORB_1('', 31),
    ORB_2('', 31),
    ORB_3('', 31),
    ORB_4('', 31),
    ORB_5('', 31),
    ORB_6('', 31),
    ORB_7('', 31),
    ORB_8('', 31),
    ORB_9('', 31),
    ORB_10('', 31),
    RUNES_PREVIOUS('', 18),
    RUNES_NEXT('', 18),
    JOBS_PREVIOUS('', 18),
    JOBS_NEXT('', 18);

    private final char glyph;
    private final int width;

    MenuImage(char glyph, int width) {
        this.glyph = glyph;
        this.width = width;
    }

    char glyph() {
        return glyph;
    }

    /** Width of the image in pixels (the glyph advances one more). */
    public int width() {
        return width;
    }

    /** The Quest note drawn in the colour of {@code tier}. */
    public static MenuImage note(Rarity tier) {
        return values()[NOTE_COMMON.ordinal() + tier.ordinal()];
    }

    /** The Capacity orb filled to {@code used / max}, in tenths (rounded down, never empty if anything is used). */
    public static MenuImage orb(long used, int max) {
        int step = max <= 0 ? 0 : (int) Math.min(10, used * 10 / max);
        if (used > 0 && step == 0) {
            step = 1;
        }
        return values()[ORB_0.ordinal() + step];
    }
}
