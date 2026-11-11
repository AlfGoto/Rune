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
}
