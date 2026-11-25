package com.alfgoto.rune.content.quests;

import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.jspecify.annotations.Nullable;

/** The wood types the Woodcutting Variety Quest counts, and which block belongs to which. */
final class WoodTypes {

    /** Every wood type: the Variety Quest asks for at most this many different ones. */
    static final List<String> IDS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "poplar",
            "crimson", "warped");

    private static final List<String> SUFFIXES = List.of("_LOG", "_WOOD", "_STEM", "_HYPHAE");

    private WoodTypes() {}

    /** The wood type of a log, wood, stem or hyphae block (stripped too), or null for any other block. */
    static @Nullable String of(Material material) {
        String name = material.name();
        if (name.startsWith("STRIPPED_")) {
            name = name.substring("STRIPPED_".length());
        }
        for (String suffix : SUFFIXES) {
            if (name.endsWith(suffix)) {
                String id = name.substring(0, name.length() - suffix.length()).toLowerCase(Locale.ROOT);
                return IDS.contains(id) ? id : null;
            }
        }
        return null;
    }

    static boolean isLog(Material material) {
        return of(material) != null;
    }
}
