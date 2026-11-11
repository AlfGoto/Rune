package com.alfgoto.rune.text;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Language files, one per module and language: {@code lang/<language>/<module>.json}
 * (e.g. {@code lang/en_us/mining.json}). Each module owns its own file, so two PRs never edit
 * the same one. They are merged into one file per language for the resource pack.
 */
public final class LangFiles {

    public static final List<String> LANGUAGES = List.of("en_us", "fr_fr");

    private static final Gson GSON = new Gson();

    private LangFiles() {}

    /** All keys of {@code language}, sorted. Throws if two files define the same key. */
    public static JsonObject merged(String language) throws IOException {
        Map<String, JsonElement> keys = new TreeMap<>();
        Map<String, String> origin = new HashMap<>();
        for (Map.Entry<String, String> file : read(language).entrySet()) {
            JsonObject json = GSON.fromJson(file.getValue(), JsonObject.class);
            if (json == null) {
                continue;
            }
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String previous = origin.putIfAbsent(entry.getKey(), file.getKey());
                if (previous != null) {
                    throw new IOException("Translation key '" + entry.getKey() + "' is in both lang/" + language
                            + "/" + previous + " and lang/" + language + "/" + file.getKey());
                }
                keys.put(entry.getKey(), entry.getValue());
            }
        }
        JsonObject result = new JsonObject();
        keys.forEach(result::add);
        return result;
    }

    /** File name → content for every {@code lang/<language>/*.json}, sorted by file name. */
    private static Map<String, String> read(String language) throws IOException {
        URL dir = LangFiles.class.getResource("/lang/" + language);
        try {
            if (dir != null && "file".equals(dir.getProtocol())) {
                return readDir(Path.of(dir.toURI()));
            }
            // Inside the plugin jar.
            Path jar = Path.of(LangFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (FileSystem fs = FileSystems.newFileSystem(jar)) {
                return readDir(fs.getPath("lang", language));
            }
        } catch (URISyntaxException e) {
            throw new IOException("Cannot locate lang/" + language, e);
        }
    }

    private static Map<String, String> readDir(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IOException("Missing language folder: " + dir);
        }
        List<Path> files = new ArrayList<>();
        try (Stream<Path> list = Files.list(dir)) {
            list.filter(p -> p.getFileName().toString().endsWith(".json")).forEach(files::add);
        }
        files.sort((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()));
        Map<String, String> result = new TreeMap<>();
        for (Path file : files) {
            result.put(file.getFileName().toString(), Files.readString(file, StandardCharsets.UTF_8));
        }
        return result;
    }
}
