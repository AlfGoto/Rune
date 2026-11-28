package com.alfgoto.rune.gameplay.durability;

import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.function.DoubleSupplier;

/** The rule of the Sturdy Rune (docs/jobs/mining.md), with no Bukkit in it so it can be tested. */
public final class Sturdy {

    /** Effect id of Sturdy: less durability use, in percent. */
    public static final String ID = "sturdy";

    private Sturdy() {}

    /**
     * The durability a tool really loses. Each point of {@code damage} is spared with the chance
     * {@code percent} (like vanilla Unbreaking), so 25% over many uses is 25% less durability use.
     * 100% or more spares everything.
     *
     * @param random gives a number in [0, 1)
     */
    public static int damage(int damage, double percent, DoubleSupplier random) {
        if (damage <= 0 || percent == 0 || !Double.isFinite(percent)) {
            return Math.max(0, damage);
        }
        if (percent < 0) {
            // A drawback (more durability use): each point costs one more with the chance -percent.
            int total = damage;
            for (int i = 0; i < damage; i++) {
                total += Stacking.rolls(-percent, random);
            }
            return total;
        }
        double spare = Math.min(100, percent);
        int kept = 0;
        for (int i = 0; i < damage; i++) {
            if (random.getAsDouble() * 100 >= spare) {
                kept++;
            }
        }
        return kept;
    }
}
