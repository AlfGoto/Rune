package com.alfgoto.rune.visuals.menu;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Draws a whole menu in its title (docs/art-direction.md, "Menus"): the background, live images, text and
 * bars, each at a pixel position of the 176x222 chest window. The title is one line of glyphs, so every
 * piece is placed by moving an invisible cursor with space glyphs of the {@code rune:menus} font, and
 * drawn with a font whose glyphs sit at the wanted height ({@link MenuFont#key}).
 *
 * <p>Clicks still happen on slots: a drawn button is covered by invisible items (see {@link Menu}).
 */
public final class MenuCanvas {

    /** The font of backgrounds, live images and cursor moves. */
    public static final Key FONT = Key.key("rune", "menus");

    /** x of the title in the chest window: where the cursor starts. */
    static final int TITLE_X = 8;

    /** First space glyph moving left 1, 2, 4 .. 256 px; the same + 0x20 moves right. */
    static final char SHIFT_LEFT = '';

    static final char SHIFT_RIGHT = '';

    /** First bar piece: style k, width w (1..16) is {@code BAR_FIRST + 16 k + w - 1}. */
    static final char BAR_FIRST = '';

    static final int BAR_PIECE = 16;

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    /** Colours of progress bars, in the order of the bar glyph sheet. */
    public enum Bar {
        GREEN,
        AQUA,
        GOLD,
        PURPLE
    }

    private final TextComponent.Builder out = Component.text();
    private int cursor = TITLE_X;

    private MenuCanvas() {}

    /** A canvas with {@code background} drawn over the whole window. */
    public static MenuCanvas on(MenuBackground background) {
        MenuCanvas canvas = new MenuCanvas();
        canvas.glyph(0, background.glyph(), MenuBackground.WIDTH);
        return canvas;
    }

    /** Draws {@code image} with its left edge at {@code x} (its height is fixed by the font). */
    public MenuCanvas image(MenuImage image, int x) {
        return glyph(x, image.glyph(), image.width());
    }

    /**
     * Draws one line of {@code text} with the top of its capitals at {@code y}. Text wider than
     * {@code maxWidth} switches to {@link MenuFont#SMALL}, then is cut with "..". {@code maxWidth} ≤ 0: no limit.
     */
    public MenuCanvas text(
            int x, int y, String text, TextColor color, MenuFont font, Align align, int maxWidth) {
        MenuFont used = font;
        String shown = text;
        if (maxWidth > 0 && font.width(shown) > maxWidth && font != MenuFont.SMALL) {
            used = MenuFont.SMALL;
        }
        if (maxWidth > 0 && used.width(shown) > maxWidth) {
            shown = cut(used, shown, maxWidth);
        }
        String normalized = used.normalize(shown);
        int width = used.width(normalized);
        int start =
                switch (align) {
                    case LEFT -> x;
                    case CENTER -> x - width / 2;
                    case RIGHT -> x - width;
                };
        moveTo(start);
        out.append(Component.text(normalized).font(used.key(y)).color(color));
        cursor = start + used.advance(normalized);
        return this;
    }

    /** {@link #text} with no width limit. */
    public MenuCanvas text(int x, int y, String text, TextColor color, MenuFont font, Align align) {
        return text(x, y, text, color, font, align, 0);
    }

    /**
     * {@code text} wrapped on words into at most {@code maxLines} lines of {@code maxWidth}, one every
     * {@code lineHeight} px from {@code y}. Falls back to {@link MenuFont#SMALL} when it does not fit.
     */
    public MenuCanvas lines(
            int x,
            int y,
            int lineHeight,
            int maxLines,
            String text,
            TextColor color,
            MenuFont font,
            Align align,
            int maxWidth) {
        MenuFont used = font;
        List<String> lines = wrap(used, text, maxWidth);
        if (lines.size() > maxLines && font != MenuFont.SMALL) {
            used = MenuFont.SMALL;
            lines = wrap(used, text, maxWidth);
        }
        for (int i = 0; i < Math.min(maxLines, lines.size()); i++) {
            String line = lines.get(i);
            if (i == maxLines - 1 && lines.size() > maxLines) {
                line = cut(used, line + " " + String.join(" ", lines.subList(i + 1, lines.size())), maxWidth);
            }
            text(x, y + i * lineHeight, line, color, used, align, maxWidth);
        }
        return this;
    }

    /** A progress bar fill of {@code fraction} (0..1) of {@code width} px, 3 px tall, top-left at (x, y). */
    public MenuCanvas bar(int x, int y, int width, double fraction, Bar style) {
        int fill = (int) Math.round(width * Math.max(0, Math.min(1, fraction)));
        int at = x;
        while (fill > 0) {
            int piece = Math.min(BAR_PIECE, fill);
            moveTo(at);
            char glyph = (char) (BAR_FIRST + BAR_PIECE * style.ordinal() + piece - 1);
            out.append(Component.text(glyph).font(MenuFont.BARS.key(y)).color(NamedTextColor.WHITE));
            cursor = at + piece + 1;
            at += piece;
            fill -= piece;
        }
        return this;
    }

    /** The finished title. */
    public Component build() {
        return out.build();
    }

    /** Where the next piece would start, in window pixels (for tests). */
    int cursor() {
        return cursor;
    }

    private MenuCanvas glyph(int x, char glyph, int width) {
        moveTo(x);
        // White: a bitmap glyph is multiplied by the text colour, and menu titles are dark grey by default.
        out.append(Component.text(glyph).font(FONT).color(NamedTextColor.WHITE));
        cursor = x + width + 1;
        return this;
    }

    private void moveTo(int x) {
        int distance = x - cursor;
        if (distance != 0) {
            out.append(Component.text(shift(distance)).font(FONT));
        }
        cursor = x;
    }

    /** Space glyphs moving the cursor by {@code px} (negative = left), as powers of two. */
    static String shift(int px) {
        StringBuilder glyphs = new StringBuilder();
        char base = px < 0 ? SHIFT_LEFT : SHIFT_RIGHT;
        int left = Math.abs(px);
        while (left > 0) {
            for (int k = 8; k >= 0; k--) {
                if (left >= (1 << k)) {
                    glyphs.append((char) (base + k));
                    left -= 1 << k;
                }
            }
        }
        return glyphs.toString();
    }

    private static List<String> wrap(MenuFont font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String current = "";
        for (String word : text.trim().split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (current.isEmpty() || font.width(candidate) <= maxWidth) {
                current = candidate;
            } else {
                lines.add(current);
                current = word;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
    }

    private static String cut(MenuFont font, String text, int maxWidth) {
        String shown = text;
        while (!shown.isEmpty() && font.width(shown + "..") > maxWidth) {
            shown = shown.substring(0, shown.length() - 1).stripTrailing();
        }
        return shown + "..";
    }
}
