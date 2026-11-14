package com.alfgoto.rune.gameplay.effects;

import java.util.List;
import java.util.Set;
import java.util.function.DoubleSupplier;

/**
 * Stacking rules (CONTEXT.md, "Stacking"): the same effect from a Trait and Runes adds up, and chances above
 * 100% roll again.
 *
 * <p>Chance values are in percent, like Rune values: 100 is a guaranteed trigger.
 */
public final class Stacking {

    /** Effect id of Silk Touch. It is on/off and turns off the drop-changing effects below on its tool. */
    public static final String SILK_TOUCH = "silk_touch";

    /** Effect ids that Silk Touch turns off on the same tool: Fortune, Smelt, Ore Bloom. */
    public static final Set<String> DISABLED_BY_SILK_TOUCH = Set.of("fortune", "smelt", "ore_bloom");

    private Stacking() {}

    /** One source of an effect: a Trait or an installed Rune, with its Rank. */
    public record Source(EffectBinding<?> binding, int rank) {

        public String effectId() {
            return binding.effect().id();
        }

        public double value() {
            return binding.valueAt(rank);
        }
    }

    /**
     * The sum of the values of every source of {@code effectId} on one tool, armor piece or player.
     * Silk Touch among the sources turns off Fortune, Smelt and Ore Bloom (the total is 0).
     */
    public static double total(String effectId, List<Source> sources) {
        if (isDisabled(effectId, sources)) {
            return 0;
        }
        double sum = 0;
        for (Source source : sources) {
            if (source.effectId().equals(effectId)) {
                sum += source.value();
            }
        }
        return sum;
    }

    /** True if Silk Touch is among {@code sources} and {@code effectId} is one of the effects it turns off. */
    public static boolean isDisabled(String effectId, List<Source> sources) {
        return DISABLED_BY_SILK_TOUCH.contains(effectId)
                && sources.stream().anyMatch(s -> s.effectId().equals(SILK_TOUCH));
    }

    /**
     * How many times a chance effect triggers. Each full 100% is a guaranteed trigger, and the rest is the
     * chance of one more. 150 gives 1, plus 1 more half of the time.
     *
     * @param percent the stacked chance, in percent; zero or less never triggers
     * @param random gives a number in [0, 1)
     */
    public static int rolls(double percent, DoubleSupplier random) {
        if (!(percent > 0)) {
            return 0;
        }
        double full = Math.floor(percent / 100);
        double rest = percent - full * 100;
        int count = (int) full;
        if (rest > 0 && random.getAsDouble() * 100 < rest) {
            count++;
        }
        return count;
    }
}
