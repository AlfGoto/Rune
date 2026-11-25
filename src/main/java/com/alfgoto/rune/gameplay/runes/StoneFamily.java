package com.alfgoto.rune.gameplay.runes;

import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;

/**
 * The stone-family blocks and ores that the ore Runes (Ore Sense, Ore Bloom) work with
 * (docs/jobs/mining.md): stone, deepslate, granite, diorite, andesite, tuff and netherrack.
 */
public final class StoneFamily {

    private static final Set<Material> OVERWORLD_STONE = Set.of(
            Material.STONE,
            Material.DEEPSLATE,
            Material.GRANITE,
            Material.DIORITE,
            Material.ANDESITE,
            Material.TUFF);

    private static final Set<Material> NETHER_ORES = Set.of(Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE);

    private StoneFamily() {}

    /** True for stone, deepslate, granite, diorite, andesite, tuff and netherrack. */
    public static boolean isStone(Material material) {
        return OVERWORLD_STONE.contains(material) || material == Material.NETHERRACK;
    }

    /** True for any ore block (including deepslate and Nether variants) and ancient debris. */
    public static boolean isOre(Material material) {
        return material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    /** True if both are ores of the same kind: deepslate iron ore and iron ore are the same vein (Veinminer). */
    public static boolean sameOre(Material a, Material b) {
        return isOre(a) && isOre(b) && plainName(a).equals(plainName(b));
    }

    private static String plainName(Material ore) {
        String name = ore.name();
        return name.startsWith("DEEPSLATE_") ? name.substring("DEEPSLATE_".length()) : name;
    }

    /**
     * The ore a neighbouring {@code stone} becomes when {@code ore} blooms into it: the same ore, in its deepslate
     * variant if the stone is deepslate, in its Nether variant if the stone is netherrack. Empty when that ore has
     * no variant for that stone (iron never grows in netherrack, Nether gold never in stone) or {@code stone} is
     * not stone-family.
     */
    public static Optional<Material> bloomInto(Material ore, Material stone) {
        if (!isStone(stone) || !isOre(ore) || ore == Material.ANCIENT_DEBRIS) {
            return Optional.empty();
        }
        if (NETHER_ORES.contains(ore)) {
            return stone == Material.NETHERRACK ? Optional.of(ore) : Optional.empty();
        }
        if (stone == Material.NETHERRACK) {
            return Optional.empty();
        }
        String name = ore.name();
        String plain = name.startsWith("DEEPSLATE_") ? name.substring("DEEPSLATE_".length()) : name;
        return Optional.ofNullable(Material.getMaterial(stone == Material.DEEPSLATE ? "DEEPSLATE_" + plain : plain));
    }
}
