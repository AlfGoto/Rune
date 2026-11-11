package com.alfgoto.rune.foundations.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds definitions of one kind (Jobs, Runes, gear, Quests, loot tables) by id.
 * Ids must be unique inside a registry.
 */
public final class Registry<T extends Identified> {

    private final String name;
    private final Map<String, T> entries = new LinkedHashMap<>();

    public Registry(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() {
        return name;
    }

    /** Adds a definition. Throws DuplicateIdException if its id is already used. */
    public T register(T entry) {
        Objects.requireNonNull(entry, "entry");
        String id = entry.id();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Definition id must not be blank in registry '" + name + "'");
        }
        if (entries.containsKey(id)) {
            throw new DuplicateIdException(name, id);
        }
        entries.put(id, entry);
        return entry;
    }

    public Optional<T> find(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    /** Returns the definition, or throws if the id is unknown. */
    public T get(String id) {
        T entry = entries.get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown id '" + id + "' in registry '" + name + "'");
        }
        return entry;
    }

    public boolean contains(String id) {
        return entries.containsKey(id);
    }

    public int size() {
        return entries.size();
    }

    /** All definitions, in registration order. Read-only view. */
    public Collection<T> all() {
        return Collections.unmodifiableCollection(entries.values());
    }
}
