package com.alfgoto.rune.visuals.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

class MenuCanvasTest {

    private static List<TextComponent> parts(Component title) {
        return title.children().stream().map(TextComponent.class::cast).toList();
    }

    /** Sum of the moves of the space glyphs in {@code shift}. */
    private static int moved(String shift) {
        int total = 0;
        for (char c : shift.toCharArray()) {
            int k = c >= MenuCanvas.SHIFT_RIGHT ? c - MenuCanvas.SHIFT_RIGHT : c - MenuCanvas.SHIFT_LEFT;
            total += (c >= MenuCanvas.SHIFT_RIGHT ? 1 : -1) * (1 << k);
        }
        return total;
    }

    @Test
    void shiftMovesExactly() {
        for (int px : new int[] {1, -1, 7, -8, -169, 255, 300, -511, 600}) {
            assertEquals(px, moved(MenuCanvas.shift(px)), "shift " + px);
        }
    }

    @Test
    void backgroundStartsAtTheLeftEdgeOfTheWindow() {
        List<TextComponent> parts = parts(MenuCanvas.on(MenuBackground.QUESTS).build());
        assertEquals(-MenuCanvas.TITLE_X, moved(parts.get(0).content()));
        assertEquals(String.valueOf(MenuBackground.QUESTS.glyph()), parts.get(1).content());
        assertEquals(MenuCanvas.FONT, parts.get(1).font());
        assertEquals(NamedTextColor.WHITE, parts.get(1).color());
    }

    @Test
    void textIsPlacedAtItsXAndDrawnWithTheFontOfItsHeight() {
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.JOBS);
        int afterBackground = canvas.cursor();
        canvas.text(30, 40, "Hello", NamedTextColor.RED, MenuFont.NORMAL, MenuCanvas.Align.LEFT);
        List<TextComponent> parts = parts(canvas.build());
        TextComponent move = parts.get(2);
        TextComponent text = parts.get(3);
        assertEquals(30 - afterBackground, moved(move.content()));
        assertEquals("Hello", text.content());
        assertEquals(Key.key("rune", "menu/normal_40"), text.font());
        assertEquals(30 + MenuFont.NORMAL.advance("Hello"), canvas.cursor());
    }

    @Test
    void centredTextStartsHalfItsWidthLeft() {
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.JOBS);
        canvas.text(88, 6, "Jobs", NamedTextColor.WHITE, MenuFont.SHADOW, MenuCanvas.Align.CENTER);
        int width = MenuFont.SHADOW.width("Jobs");
        assertEquals(88 - width / 2 + MenuFont.SHADOW.advance("Jobs"), canvas.cursor());
    }

    @Test
    void tooWideTextSwitchesToSmallCapitals() {
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.CODEX);
        canvas.text(10, 20, "A very long chapter name", NamedTextColor.WHITE, MenuFont.NORMAL,
                MenuCanvas.Align.LEFT, 44);
        TextComponent text = parts(canvas.build()).get(3);
        assertEquals(Key.key("rune", "menu/small_20"), text.font());
        assertTrue(MenuFont.SMALL.width(text.content()) <= 44, text.content());
    }

    @Test
    void barIsMadeOfPiecesThatTouch() {
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.CODEX);
        canvas.bar(19, 82, 40, 1.0, MenuCanvas.Bar.GREEN);
        // 40 px = 16 + 16 + 8, each piece starts where the last one ended (the glyph gap is moved back)
        assertEquals(19 + 40 + 1, canvas.cursor());
    }

    @Test
    void smallFontUpperCasesAndDropsAccents() {
        assertEquals("CAPACITE", MenuFont.SMALL.normalize("Capacité"));
        assertEquals("Capacité", MenuFont.NORMAL.normalize("Capacité"));
        assertEquals("Quêtes", MenuFont.NORMAL.normalize("Quêtes"));
    }

    @Test
    void stringsFollowThePlayersLanguage() {
        assertEquals("fr_fr", MenuStrings.forLocale(Locale.FRANCE).language());
        assertEquals("fr_fr", MenuStrings.forLocale(Locale.CANADA_FRENCH).language());
        assertEquals("en_us", MenuStrings.forLocale(Locale.JAPAN).language());
        assertEquals("Niveau 12", MenuStrings.forLocale(Locale.FRANCE).get("rune.menu.codex.level", "Level %s", 12));
        assertEquals("Level 12", MenuStrings.forLocale(Locale.US).get("rune.menu.codex.level", "Level %s", 12));
    }

    @Test
    void packHasAFontForEveryFamilyAndHeight() {
        Map<String, String> files = MenuFont.packFiles();
        assertEquals(MenuFont.values().length * (MenuFont.MAX_Y - MenuFont.MIN_Y + 1), files.size());
        JsonObject font = JsonParser.parseString(files.get("assets/rune/font/menu/normal_40.json")).getAsJsonObject();
        JsonArray providers = font.getAsJsonArray("providers");
        JsonObject bitmap = providers.get(providers.size() - 1).getAsJsonObject();
        // capitals' top at y 40: ascent = 13 - 40 + capTop
        assertEquals(13 - 40 + MenuFont.NORMAL.capTop(), bitmap.get("ascent").getAsInt());
        assertTrue(bitmap.get("ascent").getAsInt() <= bitmap.get("height").getAsInt());
        JsonObject top = JsonParser.parseString(files.get("assets/rune/font/menu/normal_" + MenuFont.MIN_Y + ".json"))
                .getAsJsonObject();
        JsonObject first = top.getAsJsonArray("providers").get(1).getAsJsonObject();
        assertTrue(first.get("ascent").getAsInt() <= first.get("height").getAsInt(), "ascent must not exceed height");
    }

    /** The pack's rune:menus font and the enums never drift apart. */
    @Test
    void menusFontHasEveryBackgroundAndImage() throws Exception {
        var in = getClass().getResourceAsStream("/pack/assets/rune/font/menus.json");
        assertNotNull(in, "font/menus.json is missing from the pack");
        JsonObject font = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        Map<Character, JsonObject> glyphs = new HashMap<>();
        JsonObject spaces = null;
        for (JsonElement element : font.getAsJsonArray("providers")) {
            JsonObject p = element.getAsJsonObject();
            if (p.get("type").getAsString().equals("space")) {
                spaces = p.getAsJsonObject("advances");
            } else {
                glyphs.put(p.getAsJsonArray("chars").get(0).getAsString().charAt(0), p);
            }
        }
        assertNotNull(spaces);
        for (int k = 0; k < 9; k++) {
            assertEquals(-(1 << k), spaces.get(String.valueOf((char) (MenuCanvas.SHIFT_LEFT + k))).getAsInt());
            assertEquals(1 << k, spaces.get(String.valueOf((char) (MenuCanvas.SHIFT_RIGHT + k))).getAsInt());
        }
        for (MenuBackground background : MenuBackground.values()) {
            JsonObject p = glyphs.get(background.glyph());
            assertNotNull(p, background + " has no glyph");
            assertEquals(222, p.get("height").getAsInt());
            assertEquals(13, p.get("ascent").getAsInt());
        }
        for (MenuImage image : MenuImage.values()) {
            assertNotNull(glyphs.get(image.glyph()), image + " has no glyph");
        }
    }
}
