package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.config.ArmorConfig;

/**
 * How strong a Mining armor Trait is at a given height ("deeper = stronger", docs/jobs/mining.md):
 * 0 at {@code topY}, 1 at {@code bottomY}, linear in between, clamped outside.
 *
 * @param topY height where the Trait is at 0% (default 64)
 * @param bottomY height where the Trait is at 100% (default -64)
 */
public record DepthScale(int topY, int bottomY) {

    public DepthScale {
        if (topY <= bottomY) {
            throw new IllegalArgumentException("topY (" + topY + ") must be above bottomY (" + bottomY + ")");
        }
    }

    /** The scale of the config (every piece uses it for now). */
    public static DepthScale of(ArmorConfig armor) {
        return new DepthScale(armor.depthTopY(), armor.depthBottomY());
    }

    /** The Trait strength at height {@code y}, from 0.0 to 1.0. */
    public double fraction(double y) {
        double f = (topY - y) / (topY - bottomY);
        return Math.max(0.0, Math.min(1.0, f));
    }
}
