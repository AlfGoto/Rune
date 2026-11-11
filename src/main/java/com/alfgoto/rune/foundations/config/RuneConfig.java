package com.alfgoto.rune.foundations.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers read from config.yml.
 * A bad value logs a clear error and falls back to the default.
 */
public final class RuneConfig {

    public static final String LEVEL_CURVE_BASE = "jobs.level-curve.base";
    public static final String LEVEL_CURVE_EXPONENT = "jobs.level-curve.exponent";
    public static final String ACCOUNT_CURVE_BASE = "account.level-curve.base";
    public static final String ACCOUNT_CURVE_EXPONENT = "account.level-curve.exponent";
    public static final String QUEST_TIER_MULTIPLIERS = "quests.tier-multipliers";

    static final double DEFAULT_LEVEL_CURVE_BASE = 50;
    static final double DEFAULT_LEVEL_CURVE_EXPONENT = 1.3;
    static final double DEFAULT_ACCOUNT_CURVE_BASE = 200;
    static final double DEFAULT_ACCOUNT_CURVE_EXPONENT = 1.3;
    static final List<Double> DEFAULT_QUEST_TIER_MULTIPLIERS = List.of(1.0, 1.5, 2.0, 3.0, 5.0);
    /** One multiplier per Rarity. */
    static final int QUEST_TIER_COUNT = 5;

    private final double levelCurveBase;
    private final double levelCurveExponent;
    private final double accountCurveBase;
    private final double accountCurveExponent;
    private final List<Double> questTierMultipliers;

    private RuneConfig(
            double base, double exponent, double accountBase, double accountExponent, List<Double> tiers) {
        this.levelCurveBase = base;
        this.levelCurveExponent = exponent;
        this.accountCurveBase = accountBase;
        this.accountCurveExponent = accountExponent;
        this.questTierMultipliers = List.copyOf(tiers);
    }

    /** All defaults, no file needed. */
    public static RuneConfig defaults() {
        return new RuneConfig(
                DEFAULT_LEVEL_CURVE_BASE,
                DEFAULT_LEVEL_CURVE_EXPONENT,
                DEFAULT_ACCOUNT_CURVE_BASE,
                DEFAULT_ACCOUNT_CURVE_EXPONENT,
                DEFAULT_QUEST_TIER_MULTIPLIERS);
    }

    /** Reads the config. Bad or missing values are logged as errors and replaced by defaults. */
    public static RuneConfig load(ConfigurationSection section, Logger logger) {
        double base = readPositive(section, logger, LEVEL_CURVE_BASE, DEFAULT_LEVEL_CURVE_BASE);
        double exponent = readPositive(section, logger, LEVEL_CURVE_EXPONENT, DEFAULT_LEVEL_CURVE_EXPONENT);
        double accountBase = readPositive(section, logger, ACCOUNT_CURVE_BASE, DEFAULT_ACCOUNT_CURVE_BASE);
        double accountExponent =
                readPositive(section, logger, ACCOUNT_CURVE_EXPONENT, DEFAULT_ACCOUNT_CURVE_EXPONENT);
        List<Double> tiers = readTiers(section, logger);
        return new RuneConfig(base, exponent, accountBase, accountExponent, tiers);
    }

    /** XP to reach the next level: base x level^exponent. */
    public double xpToNextLevel(int level) {
        return levelCurveBase * Math.pow(level, levelCurveExponent);
    }

    public double levelCurveBase() {
        return levelCurveBase;
    }

    public double levelCurveExponent() {
        return levelCurveExponent;
    }

    public double accountCurveBase() {
        return accountCurveBase;
    }

    public double accountCurveExponent() {
        return accountCurveExponent;
    }

    /** Quest Tier multipliers, lowest Tier first. */
    public List<Double> questTierMultipliers() {
        return questTierMultipliers;
    }

    private static double readPositive(ConfigurationSection s, Logger log, String path, double fallback) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        Object raw = s.get(path);
        if (!(raw instanceof Number n)) {
            log.severe("Config '" + path + "' must be a number, got '" + raw + "'. Using default " + fallback + ".");
            return fallback;
        }
        double value = n.doubleValue();
        if (!Double.isFinite(value) || value <= 0) {
            log.severe("Config '" + path + "' must be greater than 0, got " + value + ". Using default "
                    + fallback + ".");
            return fallback;
        }
        return value;
    }

    private static List<Double> readTiers(ConfigurationSection s, Logger log) {
        String path = QUEST_TIER_MULTIPLIERS;
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + DEFAULT_QUEST_TIER_MULTIPLIERS + ".");
            return DEFAULT_QUEST_TIER_MULTIPLIERS;
        }
        List<?> raw = s.getList(path);
        if (raw == null || raw.size() != QUEST_TIER_COUNT) {
            log.severe("Config '" + path + "' must be a list of " + QUEST_TIER_COUNT + " numbers, got '"
                    + s.get(path) + "'. Using default " + DEFAULT_QUEST_TIER_MULTIPLIERS + ".");
            return DEFAULT_QUEST_TIER_MULTIPLIERS;
        }
        List<Double> out = new ArrayList<>();
        for (Object item : raw) {
            if (!(item instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() <= 0) {
                log.severe("Config '" + path + "' must only hold numbers greater than 0, got '" + item
                        + "'. Using default " + DEFAULT_QUEST_TIER_MULTIPLIERS + ".");
                return DEFAULT_QUEST_TIER_MULTIPLIERS;
            }
            out.add(n.doubleValue());
        }
        return out;
    }
}
