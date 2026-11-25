package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.foundations.config.CacheConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.Optional;

/**
 * The pure rules of Cache drops (docs/jobs/mining.md): the drop chance of a Job Action and the Rarity of
 * the Milestone Cache of a Job Level. No Bukkit here, so tests need no server.
 */
public final class CacheDrops {

    private final CacheConfig config;

    public CacheDrops(CacheConfig config) {
        this.config = config;
    }

    /** Chance (0 to 1) that a Job Action worth {@code xp} drops a Cache: XP x percent / 100. */
    public double chance(double xp) {
        if (!(xp > 0)) {
            return 0;
        }
        return Math.min(1, xp * config.dropPercentPerXp() / 100);
    }

    /**
     * Same as {@link #chance(double)} with Cache Hunter: {@code bonusPercent} is the stacked value of the
     * Rune, a relative bonus (15 makes the chance 1.15 times bigger).
     */
    public double chance(double xp, double bonusPercent) {
        return Math.min(1, chance(xp) * (1 + Math.max(0, bonusPercent) / 100));
    }

    /** The Rarity of a Cache dropped by a Job Action (config {@code caches.drop-rarity}). */
    public Rarity dropRarity() {
        return Rarity.fromId(config.dropRarity());
    }

    /**
     * The Rarity of the Milestone Cache of {@code level}, or empty when the level has none. The table decides
     * the first levels; above it, every Legendary step gives Legendary, else every Epic step gives Epic.
     */
    public Optional<Rarity> milestone(int level) {
        if (level < 1 || config.milestones().isEmpty()) {
            return Optional.empty();
        }
        int tableEnd = config.milestones().keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (level <= tableEnd) {
            return Optional.ofNullable(config.milestones().get(level)).map(Rarity::fromId);
        }
        if (level % config.legendaryEvery() == 0) {
            return Optional.of(Rarity.LEGENDARY);
        }
        return level % config.epicEvery() == 0 ? Optional.of(Rarity.EPIC) : Optional.empty();
    }
}
