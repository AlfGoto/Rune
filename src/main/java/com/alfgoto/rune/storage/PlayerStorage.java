package com.alfgoto.rune.storage;

import java.util.Optional;
import java.util.UUID;

/**
 * Foundations: stores player data (Job XP, Runes, Quests) by key.
 *
 * <p>Values are strings. Callers turn their own data into a string (a number, JSON…). This keeps
 * the interface small, so a MySQL implementation can be added later.
 *
 * <p>Keys use lowercase letters, digits and {@code . _ - /} only (e.g. {@code job/mining/xp}).
 */
public interface PlayerStorage {

    /** Returns the value stored under {@code key}, or empty if there is none. */
    Optional<String> get(UUID player, String key);

    /** Stores {@code value} under {@code key}, replacing any previous value. */
    void set(UUID player, String key, String value);

    /** Deletes the value under {@code key}. Does nothing if there is none. */
    void remove(UUID player, String key);

    /** Returns the number stored under {@code key}, or {@code defaultValue} if there is none. */
    default long getLong(UUID player, String key, long defaultValue) {
        return get(player, key).map(Long::parseLong).orElse(defaultValue);
    }

    /** Stores a number under {@code key}. */
    default void setLong(UUID player, String key, long value) {
        set(player, key, Long.toString(value));
    }
}
