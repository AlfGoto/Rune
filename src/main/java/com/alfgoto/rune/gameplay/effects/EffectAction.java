package com.alfgoto.rune.gameplay.effects;

/** What an Effect does once its Trigger fired and its Conditions passed. */
@FunctionalInterface
public interface EffectAction<E> {

    void run(EffectContext<E> context);
}
