package com.alfgoto.rune.visuals.resourcepack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds the resource pack zip.
 *
 * <p>Content: {@code pack.mcmeta}, every asset listed in {@code /pack/files.txt} (models, textures,
 * font glyphs) and the plugin language files {@code /lang/*.json}, which become
 * {@code assets/rune/lang/*.json}. The zip is deterministic: same input, same bytes, same SHA-1.
 */
public final class ResourcePackBuilder {

    /** Pack format for Minecraft 26.3 (see docs/research/resource-pack.md). Changes on each Minecraft bump. */
    static final String PACK_MCMETA = """
            {
              "pack": {
                "description": "Rune",
                "min_format": [97, 1],
                "max_format": 97
              }
            }
            """;

    static final String ASSET_INDEX = "/pack/files.txt";
    static final List<String> LANGUAGES = List.of("en_us", "fr_fr");

    private ResourcePackBuilder() {}

    /** Result of a build: the zip file and its SHA-1 (40 hex characters). */
    public record Result(Path zip, String sha1) {}

    /** Builds the pack and writes it to {@code target}. */
    public static Result build(Path target) throws IOException {
        byte[] zip = buildBytes();
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(target, zip);
        return new Result(target, sha1(zip));
    }

    static byte[] buildBytes() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            add(zip, "pack.mcmeta", PACK_MCMETA.getBytes(StandardCharsets.UTF_8));
            for (String line : new String(resource(ASSET_INDEX), StandardCharsets.UTF_8).split("\\R")) {
                String path = line.strip();
                if (!path.isEmpty()) {
                    add(zip, path, resource("/pack/" + path));
                }
            }
            for (String language : LANGUAGES) {
                add(zip, "assets/rune/lang/" + language + ".json", resource("/lang/" + language + ".json"));
            }
        }
        return bytes.toByteArray();
    }

    private static void add(ZipOutputStream zip, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(0L); // fixed time keeps the zip (and its hash) stable
        zip.putNextEntry(entry);
        zip.write(data);
        zip.closeEntry();
    }

    private static byte[] resource(String path) throws IOException {
        try (InputStream in = ResourcePackBuilder.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Missing resource: " + path);
            }
            return in.readAllBytes();
        }
    }

    static String sha1(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
