package com.alfgoto.rune.gameplay.detection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;

/** Finds ores around a block, and says which blocks are the stone family. */
public final class OreScan {

    /** The stone family: the blocks whose breaking can trigger Ore Sense (docs/jobs/mining.md, Bone). */
    private static final Set<Material> STONE_FAMILY = EnumSet.of(
            Material.STONE,
            Material.DEEPSLATE,
            Material.GRANITE,
            Material.DIORITE,
            Material.ANDESITE,
            Material.TUFF);

    private OreScan() {}

    public static boolean isStoneFamily(Material material) {
        return STONE_FAMILY.contains(material);
    }

    /** Any {@code *_ORE} block, and ancient debris. */
    public static boolean isOre(Material material) {
        return material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    /** Ores in a cube of {@code radius} blocks around {@code center}. */
    public static List<Block> find(Block center, int radius) {
        List<Block> ores = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (isOre(block.getType())) {
                        ores.add(block);
                    }
                }
            }
        }
        return ores;
    }
}
