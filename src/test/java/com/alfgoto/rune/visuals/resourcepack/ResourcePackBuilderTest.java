package com.alfgoto.rune.visuals.resourcepack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResourcePackBuilderTest {

    private static Set<String> entries(byte[] zip) throws IOException {
        Set<String> names = new HashSet<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                names.add(e.getName());
            }
        }
        return names;
    }

    @Test
    void zipHasMetaModelGlyphsAndLangFiles(@TempDir Path dir) throws IOException {
        var result = ResourcePackBuilder.build(dir.resolve("rune-pack.zip"));
        Set<String> names = entries(Files.readAllBytes(result.zip()));
        assertTrue(names.contains("pack.mcmeta"));
        assertTrue(names.contains("assets/rune/items/test_rune.json"));
        assertTrue(names.contains("assets/rune/models/item/test_rune.json"));
        assertTrue(names.contains("assets/rune/textures/item/test_rune.png"));
        assertTrue(names.contains("assets/rune/font/glyphs.json"));
        assertTrue(names.contains("assets/rune/lang/en_us.json"));
        assertTrue(names.contains("assets/rune/lang/fr_fr.json"));
    }

    @Test
    void zipIsDeterministicAndHashMatches(@TempDir Path dir) throws IOException {
        byte[] a = ResourcePackBuilder.buildBytes();
        byte[] b = ResourcePackBuilder.buildBytes();
        assertArrayEquals(a, b);
        var result = ResourcePackBuilder.build(dir.resolve("p.zip"));
        assertEquals(ResourcePackBuilder.sha1(a), result.sha1());
        assertEquals(40, result.sha1().length());
    }
}
