package com.alfgoto.rune.gameplay.effects;

import java.util.Objects;

/** An Effect together with its values per Rank. What a Rune or a Trait holds. */
public final class EffectBinding<E> {

    private final Effect<E> effect;
    private final RankValues values;

    EffectBinding(Effect<E> effect, RankValues values) {
        this.effect = Objects.requireNonNull(effect, "effect");
        this.values = Objects.requireNonNull(values, "values");
    }

    public Effect<E> effect() {
        return effect;
    }

    public RankValues values() {
        return values;
    }

    /** The value at {@code rank}. */
    public double valueAt(int rank) {
        return values.at(rank);
    }

    /**
     * Runs the Effect for {@code event} using the value of {@code rank}.
     *
     * @return true if every Condition passed and the action ran
     */
    public boolean run(E event, int rank) {
        Objects.requireNonNull(event, "event");
        return effect.run(event, rank, values.at(rank));
    }
}
