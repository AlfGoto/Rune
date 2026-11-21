package com.alfgoto.rune.gameplay.detection;

import com.alfgoto.rune.gameplay.runes.StoneFamily;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;

/** Finds ores around a block (what an ore is: {@link StoneFamily#isOre}). */
public final class OreScan {

    private OreScan() {}

    /** Ores in a cube of {@code radius} blocks around {@code center}. */
    public static List<Block> find(Block center, int radius) {
        List<Block> ores = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (StoneFamily.isOre(block.getType())) {
                        ores.add(block);
                    }
                }
            }
        }
        return ores;
    }
}
