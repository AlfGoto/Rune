package com.alfgoto.rune.gameplay.drops;

import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.UnaryOperator;

/**
 * The rules of the drop-changing Runes (Fortune, Smelt, Silk Touch), with no Bukkit in them so they can be
 * tested. Stacking and the Silk Touch rule come from {@link Stacking}.
 *
 * <p>A drop is a stack: a kind of item {@code T} and an amount.
 */
public final class DropRules {

    /** Effect id of Fortune: +1 drop chance, in percent. */
    public static final String FORTUNE = "fortune";

    /** Effect id of Smelt: smelt chance, in percent. */
    public static final String SMELT = "smelt";

    /** On/off: any value above 0 counts as installed. */
    public static final String SILK_TOUCH = Stacking.SILK_TOUCH;

    /** One dropped stack. */
    public record Stack<T>(T item, int amount) {}

    /** What the drops of one block break become. */
    public enum Mode {
        /** No drop-changing Rune: the drops stay vanilla. */
        UNCHANGED,
        /** Silk Touch is installed: the block drops itself. Fortune and Smelt are off. */
        SILK,
        /** Fortune and/or Smelt apply. */
        CHANGED
    }

    private DropRules() {}

    /** Silk Touch wins over everything else; no Fortune and no Smelt means nothing changes. */
    public static Mode mode(List<Stacking.Source> sources) {
        if (Stacking.total(SILK_TOUCH, sources) > 0) {
            return Mode.SILK;
        }
        if (Stacking.total(FORTUNE, sources) > 0 || Stacking.total(SMELT, sources) > 0) {
            return Mode.CHANGED;
        }
        return Mode.UNCHANGED;
    }

    /**
     * Fortune then Smelt. Fortune adds one item to the first stack for each roll. Smelt applies {@code smelt} to
     * every stack once per roll, so Smelt 150% on cobblestone gives stone, then maybe smooth stone. An item that
     * cannot be smelted stays as it is (log → charcoal, then it stops).
     *
     * @param smelt gives the smelted item of an item, or the item itself when it cannot be smelted
     */
    public static <T> List<Stack<T>> apply(
            List<Stacking.Source> sources, List<Stack<T>> drops, UnaryOperator<T> smelt, DoubleSupplier random) {
        List<Stack<T>> result = new ArrayList<>(drops);
        if (result.isEmpty()) {
            return result;
        }
        int bonus = Stacking.rolls(Stacking.total(FORTUNE, sources), random);
        if (bonus > 0) {
            Stack<T> first = result.get(0);
            result.set(0, new Stack<>(first.item(), first.amount() + bonus));
        }
        int smelts = Stacking.rolls(Stacking.total(SMELT, sources), random);
        for (int i = 0; i < smelts; i++) {
            boolean changed = false;
            for (int j = 0; j < result.size(); j++) {
                Stack<T> stack = result.get(j);
                T next = smelt.apply(stack.item());
                if (!next.equals(stack.item())) {
                    result.set(j, new Stack<>(next, stack.amount()));
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return result;
    }
}
