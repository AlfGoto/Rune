package com.alfgoto.rune.foundations.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of Woodcutting armor ("Rhythm", docs/jobs/woodcutting.md), read from {@code armor.rhythm}
 * in config.yml: the most charges, the reset time, and for each Trait the value it reaches at the most charges,
 * by Rarity. A bad value logs a clear error and falls back to the default.
 *
 * @param maxCharges most charges (10): the Trait is at 100% there
 * @param resetSeconds seconds without a Felled Tree after which every charge is lost (30)
 * @param maxValues Trait key -> Rarity (lower case) -> value in percent at the most charges
 */
public record RhythmConfig(int maxCharges, int resetSeconds, Map<String, Map<String, Double>> maxValues) {

    public static final String PATH = "armor.rhythm";
    public static final String CHOP_SPEED = "chop-speed";
    public static final String DAMAGE_REDUCTION = "damage-reduction";
    public static final String MOVE_SPEED = "move-speed";
    public static final String JUMP = "jump";
    public static final String FALL_REDUCTION = "fall-reduction";

    static final List<String> KEYS = List.of(CHOP_SPEED, DAMAGE_REDUCTION, MOVE_SPEED, JUMP, FALL_REDUCTION);
    static final List<String> RARITIES = List.of("common", "uncommon");

    public RhythmConfig {
        if (maxCharges < 1) {
            throw new IllegalArgumentException("Max charges must be at least 1: " + maxCharges);
        }
        if (resetSeconds < 1) {
            throw new IllegalArgumentException("Reset seconds must be at least 1: " + resetSeconds);
        }
        Map<String, Map<String, Double>> copy = new LinkedHashMap<>();
        maxValues.forEach((key, byRarity) -> copy.put(key, Map.copyOf(byRarity)));
        maxValues = Map.copyOf(copy);
    }

    /** Starting values decided by Alfred (issue #98). */
    public static RhythmConfig defaults() {
        return new RhythmConfig(
                10,
                30,
                Map.of(
                        CHOP_SPEED, Map.of("common", 15.0, "uncommon", 25.0),
                        DAMAGE_REDUCTION, Map.of("common", 8.0, "uncommon", 12.0),
                        MOVE_SPEED, Map.of("common", 8.0, "uncommon", 12.0),
                        JUMP, Map.of("common", 10.0, "uncommon", 15.0),
                        FALL_REDUCTION, Map.of("common", 20.0, "uncommon", 35.0)));
    }

    /** The value in percent a Trait reaches at the most charges for {@code rarity} (lower case); 0 if none. */
    public double max(String key, String rarity) {
        return maxValues.getOrDefault(key, Map.of()).getOrDefault(rarity, 0.0);
    }

    /** Reads {@code armor.rhythm}. Bad or missing values are logged and replaced by defaults. */
    public static RhythmConfig load(ConfigurationSection root, Logger logger) {
        RhythmConfig d = defaults();
        ConfigurationSection section = root.getConfigurationSection(PATH);
        if (section == null) {
            logger.severe("Config '" + PATH + "' is missing. Using defaults.");
            return d;
        }
        int charges = readWhole(section, logger, "max-charges", d.maxCharges);
        int reset = readWhole(section, logger, "reset-seconds", d.resetSeconds);
        Map<String, Map<String, Double>> values = new LinkedHashMap<>();
        for (String key : KEYS) {
            Map<String, Double> byRarity = new LinkedHashMap<>();
            for (String rarity : RARITIES) {
                String path = key + "." + rarity;
                double fallback = d.max(key, rarity);
                Object raw = section.get(path);
                if (raw instanceof Number n && Double.isFinite(n.doubleValue()) && n.doubleValue() >= 0) {
                    byRarity.put(rarity, n.doubleValue());
                } else {
                    logger.severe("Config '" + PATH + "." + path + "' must be a number from 0 up, got '" + raw
                            + "'. Using default " + fallback + ".");
                    byRarity.put(rarity, fallback);
                }
            }
            values.put(key, byRarity);
        }
        return new RhythmConfig(charges, reset, values);
    }

    private static int readWhole(ConfigurationSection s, Logger log, String path, int fallback) {
        Object raw = s.get(path);
        if (!(raw instanceof Number n) || n.doubleValue() != Math.rint(n.doubleValue()) || n.doubleValue() < 1) {
            log.severe("Config '" + PATH + "." + path + "' must be a whole number from 1 up, got '" + raw
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        return n.intValue();
    }
}
