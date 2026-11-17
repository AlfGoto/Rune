package com.alfgoto.rune.gameplay.woodcutting;

import java.util.ArrayList;
import java.util.List;

/** The ids and the Leaf Fall shape of the leaf Runes, with no Bukkit in them so they can be tested. */
public final class LeafRules {

    /** Effect id of Leaf Fall: the radius in blocks. */
    public static final String LEAF_FALL = "leaf_fall";

    /** Effect id of Orchard: extra apple and sapling rolls, in percent. */
    public static final String ORCHARD = "orchard";

    /** Effect id of Canopy Stride: move speed on leaves and logs, in percent. */
    public static final String CANOPY_STRIDE = "canopy_stride";

    /** A block position relative to the broken log. */
    public record Offset(int x, int y, int z) {}

    private LeafRules() {}

    /** Every offset within {@code radius} blocks of the broken log (a sphere), the log itself excluded. */
    public static List<Offset> offsets(double radius) {
        List<Offset> list = new ArrayList<>();
        int max = (int) Math.floor(radius);
        double squared = radius * radius;
        for (int x = -max; x <= max; x++) {
            for (int y = -max; y <= max; y++) {
                for (int z = -max; z <= max; z++) {
                    if ((x != 0 || y != 0 || z != 0) && x * x + y * y + z * z <= squared) {
                        list.add(new Offset(x, y, z));
                    }
                }
            }
        }
        return list;
    }
}
