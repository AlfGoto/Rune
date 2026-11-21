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
                "mountains",
                "minecraft:meadow",
                "minecraft:grove",
                "minecraft:snowy_slopes",
                "minecraft:jagged_peaks",
                "minecraft:frozen_peaks",
                "minecraft:stony_peaks",
                "minecraft:windswept_hills",
                "minecraft:windswept_gravelly_hills"));
        registry.register(Place.biome(
                "ocean",
                "minecraft:ocean",
                "minecraft:deep_ocean",
                "minecraft:warm_ocean",
                "minecraft:lukewarm_ocean",
                "minecraft:deep_lukewarm_ocean",
                "minecraft:cold_ocean",
                "minecraft:deep_cold_ocean",
                "minecraft:frozen_ocean",
                "minecraft:deep_frozen_ocean"));
        registry.register(Place.biome(
                "nether",
                "minecraft:nether_wastes",
                "minecraft:soul_sand_valley",
                "minecraft:crimson_forest",
                "minecraft:warped_forest",
                "minecraft:basalt_deltas"));
        registry.register(Place.structure("mineshaft", "minecraft:mineshaft", "minecraft:mineshaft_mesa"));
    }
}
