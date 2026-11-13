package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.registry.Registry;
import java.util.Collection;
import java.util.Optional;

/** All known Effects by id. Shared as a service; content modules fill it (ADR 0004). */
public final class EffectRegistry {

    private final Registry<Effect<?>> effects = new Registry<>("effects");

    public <E> Effect<E> register(Effect<E> effect) {
        effects.register(effect);
        return effect;
    }

    public Optional<Effect<?>> find(String id) {
        return effects.find(id);
    }

    /** Returns the Effect, or throws if the id is unknown. */
    public Effect<?> get(String id) {
        return effects.get(id);
    }

    /** All Effects, in registration order. Read it when you use it, not at enable time. */
    public Collection<Effect<?>> all() {
        return effects.all();
    }
}
