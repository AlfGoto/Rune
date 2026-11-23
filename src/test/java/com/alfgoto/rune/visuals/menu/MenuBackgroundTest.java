package com.alfgoto.rune.visuals.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

class MenuBackgroundTest {

    @Test
    void titleIsShiftedBackgroundThenTheTitle() {
        Component title = Component.text("Quests");
        Component full = MenuBackground.QUESTS.title(title);
        TextComponent glyphs = (TextComponent) full.children().get(0);
        assertEquals("", glyphs.content());
        assertEquals(MenuBackground.FONT, glyphs.font());
        assertEquals(NamedTextColor.WHITE, glyphs.color());
        assertEquals(title, full.children().get(1));
    }

    /** The pack's rune:menus font and this enum never drift apart. */
    @Test
    void packFontHasEveryBackgroundAtTheRightHeight() throws Exception {
        var in = getClass().getResourceAsStream("/pack/assets/rune/font/menus.json");
        assertNotNull(in, "font/menus.json is missing from the pack");
        JsonObject font = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray providers = font.getAsJsonArray("providers");
        JsonObject space = providers.get(0).getAsJsonObject().getAsJsonObject("advances");
        assertEquals(-8, space.get(String.valueOf(MenuBackground.SHIFT_LEFT)).getAsInt());
        assertEquals(-169, space.get(String.valueOf(MenuBackground.SHIFT_BACK)).getAsInt());
        for (MenuBackground background : MenuBackground.values()) {
            String glyph = ((TextComponent) background.title(Component.empty()).children().get(0))
                    .content()
                    .substring(1, 2);
            JsonObject provider = null;
            for (JsonElement element : providers) {
                JsonObject p = element.getAsJsonObject();
                if (p.has("chars") && p.getAsJsonArray("chars").get(0).getAsString().equals(glyph)) {
                    provider = p;
                }
            }
            assertNotNull(provider, background + " has no glyph in font/menus.json");
            assertEquals(114 + 18 * background.rows(), provider.get("height").getAsInt(), background.name());
            assertEquals(13, provider.get("ascent").getAsInt(), background.name());
        }
    }
}
