package com.alfgoto.rune.foundations.config;

import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of the mining speed Runes, read from the {@code runes.speed} section of config.yml:
 * the speed per Rank (in %) and the Max Rank of Haste, Specialist and Affinity (docs/jobs/mining.md).
 * A bad value logs a clear error and falls back to the default.
 */
public record SpeedRuneConfig(
        double hastePerRank,
        int hasteMaxRank,
        double specialistPerRank,
        int specialistMaxRank,
        double affinityPerRank,
        int affinityMaxRank) {

    public static final String HASTE = "runes.speed.haste";
    public static final String SPECIALIST = "runes.speed.specialist";
    public static final String AFFINITY = "runes.speed.affinity";

    public static SpeedRuneConfig defaults() {
        return new SpeedRuneConfig(8, 5, 20, 3, 10, 3);
    }

    /** Reads the {@code runes.speed} section. Bad or missing values are logged and replaced by defaults. */
    public static SpeedRuneConfig load(ConfigurationSection section, Logger logger) {
        SpeedRuneConfig d = defaults();
        return new SpeedRuneConfig(
                number(section, logger, HASTE + ".per-rank", d.hastePerRank),
                (int) whole(section, logger, HASTE + ".max-rank", d.hasteMaxRank),
                number(section, logger, SPECIALIST + ".per-rank", d.specialistPerRank),
                (int) whole(section, logger, SPECIALIST + ".max-rank", d.specialistMaxRank),
                number(section, logger, AFFINITY + ".per-rank", d.affinityPerRank),
                (int) whole(section, logger, AFFINITY + ".max-rank", d.affinityMaxRank));
    }

    private static double number(ConfigurationSection s, Logger log, String path, double fallback) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        Object raw = s.get(path);
        if (!(raw instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() <= 0) {
            log.severe("Config '" + path + "' must be a number greater than 0, got '" + raw + "'. Using default "
                    + fallback + ".");
            return fallback;
        }
        return n.doubleValue();
    }

    private static double whole(ConfigurationSection s, Logger log, String path, double fallback) {
        double value = number(s, log, path, fallback);
        if (value != Math.rint(value) || value > 1000) {
            log.severe("Config '" + path + "' must be a whole number from 1 to 1000, got '" + value
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        return value;
    }
}
