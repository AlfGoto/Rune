package com.alfgoto.rune.content.places;

import com.alfgoto.rune.gameplay.places.Place;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;

/**
 * The Places named in {@code docs/jobs/mining.md} and {@code docs/jobs/woodcutting.md}
 * that are decided so far. More are added when their Job content is built.
 */
public final class CorePlaces {

    private CorePlaces() {}

    public static void registerAll(PlaceRegistry registry) {
        registry.register(Place.biome("lush_caves", "minecraft:lush_caves"));
        registry.register(Place.biome("taiga", "minecraft:taiga", "minecraft:snowy_taiga"));
        registry.register(Place.structure("mineshaft", "minecraft:mineshaft", "minecraft:mineshaft_mesa"));
    }
}
