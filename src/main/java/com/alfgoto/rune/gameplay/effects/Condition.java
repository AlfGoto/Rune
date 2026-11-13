package com.alfgoto.rune.gameplay.effects;

/** A test an Effect must pass before its action runs. */
@FunctionalInterface
public interface Condition<E> {

    boolean test(EffectContext<E> context);
}
