package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.text.Rarity;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * What a roll knows: who it is for, the source Rarity (a Rune Cache's Rarity or a Quest Tier) and
 * optionally a Job (a Cache dropped by a Job leans towards that Job's Runes).
 * The Job is an id, so Rewards does not import Jobs.
 */
public record RollContext(UUID player, Rarity rarity, @Nullable String jobId) {

    public RollContext {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(rarity, "rarity");
    }

    /** A context with no Job. */
    public static RollContext of(UUID player, Rarity rarity) {
        return new RollContext(player, rarity, null);
    }

    public Optional<String> job() {
        return Optional.ofNullable(jobId);
    }
}
