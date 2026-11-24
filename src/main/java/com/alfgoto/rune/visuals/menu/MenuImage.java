package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.text.Rarity;

/**
 * A live image of the {@code rune:menus} font, drawn by {@link MenuCanvas#image}: its height is fixed in
 * the font, its x is chosen when drawing. Textures come from {@code tools/texture-gen/v3/menus.py}.
 */
public enum MenuImage {
    NOTE_COMMON('\ue110', 52),
    NOTE_UNCOMMON('\ue111', 52),
    NOTE_RARE('\ue112', 52),
    NOTE_EPIC('\ue113', 52),
    NOTE_LEGENDARY('\ue114', 52),
    ORB_0('\ue120', 31),
    ORB_1('\ue121', 31),
    ORB_2('\ue122', 31),
    ORB_3('\ue123', 31),
    ORB_4('\ue124', 31),
    ORB_5('\ue125', 31),
    ORB_6('\ue126', 31),
    ORB_7('\ue127', 31),
    ORB_8('\ue128', 31),
    ORB_9('\ue129', 31),
    ORB_10('\ue12a', 31),
    RUNES_PREVIOUS('\ue130', 18),
    RUNES_NEXT('\ue131', 18),
    JOBS_PREVIOUS('\ue132', 18),
    JOBS_NEXT('\ue133', 18),
    JOB_CARD_TOP('\ue140', 53),
    JOB_CARD_TOP_LOCKED('\ue141', 53),
    JOB_CARD_BOTTOM('\ue142', 53),
    JOB_CARD_BOTTOM_LOCKED('\ue143', 53),
    REROLL('\ue150', 18);

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
