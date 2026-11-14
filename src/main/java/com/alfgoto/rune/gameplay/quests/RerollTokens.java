package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.storage.PlayerStorage;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Reroll Tokens: a player earns one per world day and holds at most {@code quests.max-reroll-tokens}.
 * Stored under {@code quest/reroll/tokens} and {@code quest/reroll/day} (the world day tokens were
 * last counted on). Tokens are counted lazily: days that passed since then are added when read.
 * A new player starts with 0 Reroll Tokens.
 */
public final class RerollTokens {

    private static final String TOKENS_KEY = "quest/reroll/tokens";
    private static final String DAY_KEY = "quest/reroll/day";

    private final PlayerStorage storage;
    private final RuneConfig config;
    private final LongSupplier worldDay;

    /** @param worldDay the current world day (the main world's full time / 24000) */
    public RerollTokens(PlayerStorage storage, RuneConfig config, LongSupplier worldDay) {
        this.storage = storage;
        this.config = config;
        this.worldDay = worldDay;
    }

    /** How many Reroll Tokens the player holds now, after adding the ones earned since last time. */
    public int count(UUID player) {
        long today = worldDay.getAsLong();
        Long lastDay = readLong(player, DAY_KEY);
        if (lastDay == null) {
            write(player, 0, today);
            return 0;
        }
        int held = Math.max(0, (int) Math.min(readInt(player, TOKENS_KEY), Integer.MAX_VALUE));
        long earned = today - lastDay;
        if (earned == 0 && held <= config.questMaxRerollTokens()) {
            return held;
        }
        // Time set back (earned < 0) earns nothing but moves the day, so tokens come back after it.
        long total = Math.min(config.questMaxRerollTokens(), held + Math.max(0, earned));
        write(player, (int) total, today);
        return (int) total;
    }

    /** Spends one Reroll Token. Returns false (and spends nothing) if the player has none. */
    public boolean spend(UUID player) {
        int held = count(player);
        if (held <= 0) {
            return false;
        }
        storage.set(player, TOKENS_KEY, Integer.toString(held - 1));
        return true;
    }

    private void write(UUID player, int tokens, long day) {
        storage.set(player, TOKENS_KEY, Integer.toString(tokens));
        storage.set(player, DAY_KEY, Long.toString(day));
    }

    private Long readLong(UUID player, String key) {
        try {
            return storage.get(player, key).map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private long readInt(UUID player, String key) {
        Long value = readLong(player, key);
        return value == null ? 0 : value;
    }
}
