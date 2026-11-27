package com.alfgoto.rune.content.items;

/**
 * The effect ids of the biome pickaxe Traits (Lichen, Fiery, Emerald, Prismarine). The items use them in their
 * Trait bindings; {@link BiomePickaxeRulesModule} registers the rules that say where each one applies.
 * The Affinity ids start with {@code trait_} so a future Affinity Rune of the same Place never clashes.
 */
public final class BiomePickaxeTraits {

    private BiomePickaxeTraits() {}

    /** Lichen uses the existing Affinity: Lush Caves speed effect. */
    public static final String AFFINITY_LUSH_CAVES = "affinity_lush_caves";

    public static final String AFFINITY_NETHER = "trait_affinity_nether";
    public static final String AFFINITY_MOUNTAINS = "trait_affinity_mountains";
    public static final String AFFINITY_OCEAN = "trait_affinity_ocean";

    /** % move speed in Mountains (Emerald). */
    public static final String MOUNTAIN_MOVE_SPEED = "trait_mountain_move_speed";
}
