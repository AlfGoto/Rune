package com.alfgoto.rune.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextColor;
import org.junit.jupiter.api.Test;

class TextTest {

    private static JsonObject load(String file) throws IOException {
        try (var in = TextTest.class.getResourceAsStream("/lang/" + file)) {
            assertNotNull(in, file);
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, JsonObject.class);
            }
        }
    }

    @Test
    void translatableHasEnglishFallback() {
        TranslatableComponent c = Text.translatable("rune.test.key", "Hello");
        assertEquals("rune.test.key", c.key());
        assertEquals("Hello", c.fallback());
    }

    @Test
    void translatableKeepsArguments() {
        TranslatableComponent c = Text.translatable("rune.test.arg", "Hi %s", Component.text("Alf"));
        assertEquals(1, c.arguments().size());
    }

    @Test
    void keyFollowsNamespace() {
        assertEquals("rune.rarity.epic", Text.key("rarity", "epic"));
    }

    @Test
    void rarityNameIsTranslatableColouredWithFallback() {
        for (Rarity rarity : Rarity.values()) {
            TranslatableComponent c = Text.rarityName(rarity);
            assertEquals(rarity.translationKey(), c.key());
            assertEquals(rarity.englishName(), c.fallback());
            assertEquals(rarity.color(), c.color());
        }
    }

    @Test
    void everyRarityHasADistinctColour() {
        Set<TextColor> colours = new HashSet<>();
        for (Rarity rarity : Rarity.values()) {
            assertTrue(colours.add(rarity.color()), rarity.name());
        }
    }

    @Test
    void glyphUsesGlyphFont() {
        assertEquals(Glyphs.FONT, Glyphs.glyph('').font());
    }

    @Test
    void languageFilesHaveTheSameKeys() throws IOException {
        assertEquals(load("en_us.json").keySet(), load("fr_fr.json").keySet());
    }

    @Test
    void englishFileMatchesRarityFallbacks() throws IOException {
        JsonObject en = load("en_us.json");
        for (Rarity rarity : Rarity.values()) {
            assertEquals(rarity.englishName(), en.get(rarity.translationKey()).getAsString());
        }
    }
}
