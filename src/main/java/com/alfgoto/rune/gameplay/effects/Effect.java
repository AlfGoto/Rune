package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.registry.Identified;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "When X happens, if Y, do Z": a Trigger, Conditions and an action. Runes and Traits are built on it.
 *
 * <p>The Effect holds no numbers. {@link #with} binds it to values per Rank.
 */
public final class Effect<E> implements Identified {

    private final String id;
    private final Trigger<E> trigger;
    private final List<Condition<E>> conditions;
    private final EffectAction<E> action;

    private Effect(String id, Trigger<E> trigger, List<Condition<E>> conditions, EffectAction<E> action) {
        this.id = id;
        this.trigger = trigger;
        this.conditions = List.copyOf(conditions);
        this.action = action;
    }

    public static <E> Builder<E> builder(String id, Trigger<E> trigger) {
        return new Builder<>(id, trigger);
    }

    @Override
    public String id() {
        return id;
    }

    public Trigger<E> trigger() {
        return trigger;
    }

    /** Binds this Effect to one value per Rank (e.g. {@code effect.with(8, 16, 32)}). */
    public EffectBinding<E> with(double... valuesPerRank) {
        return new EffectBinding<>(this, RankValues.of(valuesPerRank));
    }

    public EffectBinding<E> with(RankValues values) {
        return new EffectBinding<>(this, values);
    }

    /** Runs the Effect once. Returns true if every Condition passed and the action ran. */
    boolean run(E event, int rank, double value) {
        EffectContext<E> context = new EffectContext<>(event, trigger.playerOf(event), rank, value);
        for (Condition<E> condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        action.run(context);
        return true;
    }

    public static final class Builder<E> {

        private final String id;
        private final Trigger<E> trigger;
        private final List<Condition<E>> conditions = new ArrayList<>();

        private Builder(String id, Trigger<E> trigger) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Effect id must not be blank");
            }
            this.id = id;
            this.trigger = Objects.requireNonNull(trigger, "trigger");
        }

        /** Adds a Condition. All Conditions must pass. */
        public Builder<E> when(Condition<E> condition) {
            conditions.add(Objects.requireNonNull(condition, "condition"));
            return this;
        }

        /** Sets the action and builds the Effect. */
        public Effect<E> does(EffectAction<E> action) {
            return new Effect<>(id, trigger, conditions, Objects.requireNonNull(action, "action"));
        }
    }
}
