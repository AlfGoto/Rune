package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.text.LangFiles;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

/**
 * Plain text in a player's language, for text drawn in menus: it must be measured on the server, so it
 * cannot be a client-side translatable component. Reads the same lang files as the resource pack.
 */
public final class MenuStrings {

    private static final Map<String, JsonObject> CACHE = new ConcurrentHashMap<>();

    private final String language;

    private MenuStrings(String language) {
        this.language = language;
    }

    /** The plugin language closest to the player's client language (English if none matches). */
    public static MenuStrings of(Player player) {
        return forLocale(player.locale());
    }

    public static MenuStrings forLocale(Locale locale) {
        String tag = locale == null ? "" : locale.toString().toLowerCase(Locale.ROOT);
        String language = "en_us";
        for (String candidate : LangFiles.LANGUAGES) {
            if (candidate.equals(tag)) {
                language = candidate;
                break;
            }
            if (!tag.isEmpty() && candidate.startsWith(tag.split("_")[0] + "_")) {
                language = candidate;
            }
        }
        return new MenuStrings(language);
    }

    public String language() {
        return language;
    }

    /** The text of {@code key} with {@code %s} / {@code %1$s} filled by {@code args}; {@code fallback} if missing. */
    public String get(String key, String fallback, Object... args) {
        JsonElement value = keys().get(key);
        String pattern = value == null ? fallback : value.getAsString();
        if (args.length == 0) {
            return pattern;
        }
        try {
            return String.format(Locale.ROOT, pattern, args);
        } catch (RuntimeException e) {
            return String.format(Locale.ROOT, fallback, args);
        }
    }

    private JsonObject keys() {
        return CACHE.computeIfAbsent(language, lang -> {
            try {
                return LangFiles.merged(lang);
            } catch (IOException e) {
                return new JsonObject();
            }
        });
    }
}
