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
        registry.register(Place.biome(
                "birch_forest", "minecraft:birch_forest", "minecraft:old_growth_birch_forest"));
        // The biome key is not checked on a running 26.3 server: confirm it with /rune where.
        registry.register(Place.biome("dappled_forest", "minecraft:dappled_forest"));
        registry.register(Place.biome(
                "jungle", "minecraft:jungle", "minecraft:sparse_jungle", "minecraft:bamboo_jungle"));
        registry.register(Place.structure("mineshaft", "minecraft:mineshaft", "minecraft:mineshaft_mesa"));
    }
}
