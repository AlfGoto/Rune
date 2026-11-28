package com.alfgoto.rune.gameplay.veins;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/** Finds the blocks of a tunnel: the 3x3 square around a block, facing the side the player mined. */
public final class TunnelScan {

    private TunnelScan() {}

    /**
     * The up to 8 blocks around {@code origin} in the plane of {@code face} (the side of the block that was mined,
     * so the tunnel goes into the wall) that {@code accept} says yes to. {@code origin} is not in the result.
     * A face that is not one of the 6 sides gives nothing.
     */
    public static List<Block> square(Block origin, BlockFace face, Predicate<Block> accept) {
        List<Block> found = new ArrayList<>();
        if (!face.isCartesian()) {
            return found;
        }
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                // The plane is the two axes that are not the face's axis.
                int dx = face.getModX() != 0 ? 0 : a;
                int dy = face.getModY() != 0 ? 0 : (face.getModX() != 0 ? a : b);
                int dz = face.getModZ() != 0 ? 0 : b;
                Block block = origin.getRelative(dx, dy, dz);
                if (accept.test(block)) {
                    found.add(block);
                }
            }
        }
        return found;
    }
}
