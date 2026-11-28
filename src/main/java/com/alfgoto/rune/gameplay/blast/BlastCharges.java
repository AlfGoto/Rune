package com.alfgoto.rune.gameplay.blast;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * The armed blasts of Blast Mine and Great Blast: a click opens a window for a player: every block they mine
 * with the tool during the window blasts. The window ends by itself after its time. Kept in memory only.
 */
public final class BlastCharges {

    private final LongSupplier millis;
    private final Map<UUID, Map<String, Long>> expiry = new HashMap<>();

    public BlastCharges() {
        this(System::currentTimeMillis);
    }

    /** {@code millis} is the clock (tests give their own). */
    public BlastCharges(LongSupplier millis) {
        this.millis = millis;
    }

    /** Arms {@code effectId} for {@code player} for {@code seconds}. Arming again restarts the time. */
    public void arm(UUID player, String effectId, double seconds) {
        expiry.computeIfAbsent(player, id -> new HashMap<>())
                .put(effectId, millis.getAsLong() + (long) (seconds * 1000));
    }

    /** True when {@code effectId} is armed for {@code player} and its time is not over. */
    public boolean isArmed(UUID player, String effectId) {
        Map<String, Long> charges = expiry.get(player);
        Long end = charges == null ? null : charges.get(effectId);
        if (end == null) {
            return false;
        }
        if (millis.getAsLong() >= end) {
            charges.remove(effectId);
            return false;
        }
        return true;
    }

    /** Spends the charge: true when it was armed (and is now not). */
    public boolean consume(UUID player, String effectId) {
        if (!isArmed(player, effectId)) {
            return false;
        }
        expiry.get(player).remove(effectId);
        return true;
    }

    /** Forgets every charge of {@code player} (they left). */
    public void clear(UUID player) {
        expiry.remove(player);
    }
}
