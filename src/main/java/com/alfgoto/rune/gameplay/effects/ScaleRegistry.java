package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.registry.Registry;
import java.util.Collection;
import java.util.Optional;

/** All known Scales by id. Shared as a service; content modules fill it (ADR 0004). Has {@code always}. */
public final class ScaleRegistry {

    private final Registry<Scale> scales = new Registry<>("scales");

    public ScaleRegistry() {
        scales.register(Scale.ALWAYS);
    }

    public Scale register(Scale scale) {
        return scales.register(scale);
    }

    public Optional<Scale> find(String id) {
        return scales.find(id);
    }

    /** Returns the Scale, or throws if the id is unknown. */
    public Scale get(String id) {
        return scales.get(id);
    }

    public Collection<Scale> all() {
        return scales.all();
    }
}
