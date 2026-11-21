package com.alfgoto.rune.foundations.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of Rune Cache drops, read from the {@code caches} section of config.yml
 * (docs/jobs/mining.md): the drop chance per Job XP and the Milestone Cache table.
 * Rarities are kept as ids (Foundations does not know Rarity). A bad value logs an error and falls back.
 *
 * @param dropPercentPerXp drop chance in percent per Job XP of the action (0.05: 8 XP = 0.4%)
 * @param milestones Rarity id by Job Level for the first milestones (5 common ... 30 epic)
 * @param epicEvery above the table, an Epic Cache every this many levels
 * @param legendaryEvery above the table, a Legendary Cache every this many levels (wins over Epic)
 */
public record CacheConfig(
        double dropPercentPerXp, Map<Integer, String> milestones, int epicEvery, int legendaryEvery) {

    public static final String DROP_PERCENT_PER_XP = "caches.drop-percent-per-xp";
    public static final String MILESTONES = "caches.milestones";
    public static final String EPIC_EVERY = "caches.epic-every";
    public static final String LEGENDARY_EVERY = "caches.legendary-every";

    private static final List<String> RARITIES = List.of("common", "uncommon", "rare", "epic", "legendary");

    public CacheConfig {
        milestones = Map.copyOf(milestones);
    }

    public static CacheConfig defaults() {
        return new CacheConfig(
                0.05,
                Map.of(5, "common", 10, "uncommon", 15, "rare", 20, "rare", 25, "epic", 30, "epic"),
                5,
                25);
    }

    /** Reads the {@code caches} section. Bad or missing values are logged and replaced by defaults. */
    public static CacheConfig load(ConfigurationSection section, Logger logger) {
        CacheConfig d = defaults();
        return new CacheConfig(
                number(section, logger, DROP_PERCENT_PER_XP, d.dropPercentPerXp, false),
                readMilestones(section, logger, d.milestones),
                (int) number(section, logger, EPIC_EVERY, d.epicEvery, true),
                (int) number(section, logger, LEGENDARY_EVERY, d.legendaryEvery, true));
    }

    private static Map<Integer, String> readMilestones(
            ConfigurationSection s, Logger log, Map<Integer, String> fallback) {
        ConfigurationSection table = s.getConfigurationSection(MILESTONES);
        if (table == null) {
            log.severe("Config '" + MILESTONES + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        Map<Integer, String> out = new LinkedHashMap<>();
        for (String key : table.getKeys(false)) {
            Object raw = table.get(key);
            String rarity = raw == null ? "" : raw.toString().toLowerCase(Locale.ROOT);
            int level;
            try {
                level = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                level = 0;
            }
            if (level < 1 || !RARITIES.contains(rarity)) {
                log.severe("Config '" + MILESTONES + "." + key + "' must be a Job Level and a Rarity, got '" + raw
                        + "'. Entry skipped.");
                continue;
            }
            out.put(level, rarity);
        }
        return out;
    }

    private static double number(ConfigurationSection s, Logger log, String path, double fallback, boolean whole) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        Object raw = s.get(path);
        if (!(raw instanceof Number n)
                || !Double.isFinite(n.doubleValue())
                || n.doubleValue() <= 0
                || (whole && (n.doubleValue() != Math.rint(n.doubleValue()) || n.doubleValue() > 1_000_000))) {
            log.severe("Config '" + path + "' must be a "
                    + (whole ? "whole number from 1 to 1000000" : "number greater than 0") + ", got '" + raw
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        return n.doubleValue();
    }
}
