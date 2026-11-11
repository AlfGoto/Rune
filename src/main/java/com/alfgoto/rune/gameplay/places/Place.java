package com.alfgoto.rune.gameplay.places;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.text.Text;
import java.util.Set;

/**
 * A Place: a biome or a structure a player can stand in (Lush Caves, Mineshaft, Taiga...).
 * Matches by Minecraft key, e.g. {@code minecraft:taiga}.
 */
public record Place(String id, Kind kind, Set<String> keys) implements Identified {

    /** What a Place is made of. */
    public enum Kind {
        BIOME,
        STRUCTURE
    }

    public Place {
        keys = Set.copyOf(keys);
    }

    public static Place biome(String id, String... biomeKeys) {
        return new Place(id, Kind.BIOME, Set.of(biomeKeys));
    }

    public static Place structure(String id, String... structureKeys) {
        return new Place(id, Kind.STRUCTURE, Set.of(structureKeys));
    }

    /** Translation key of the Place name: {@code rune.place.<id>}. */
    public String translationKey() {
        return Text.key("place", id);
    }
}
