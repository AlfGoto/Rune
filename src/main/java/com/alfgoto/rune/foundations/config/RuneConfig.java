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
    public static final String QUEST_TIER_MIN_LEVELS = "quests.tier-min-levels";
    public static final String QUEST_TIER_WEIGHT_BASE = "quests.tier-weights.base";
    public static final String QUEST_TIER_WEIGHT_PER_LEVEL = "quests.tier-weights.per-level";

    static final double DEFAULT_LEVEL_CURVE_BASE = 50;
    static final double DEFAULT_LEVEL_CURVE_EXPONENT = 1.3;
    static final double DEFAULT_ACCOUNT_CURVE_BASE = 200;
    static final double DEFAULT_ACCOUNT_CURVE_EXPONENT = 1.3;
    static final List<Double> DEFAULT_QUEST_TIER_MULTIPLIERS = List.of(1.0, 1.5, 2.0, 3.0, 5.0);
    static final List<Double> DEFAULT_QUEST_TIER_MIN_LEVELS = List.of(1.0, 1.0, 1.0, 15.0, 25.0);
    static final List<Double> DEFAULT_QUEST_TIER_WEIGHT_BASE = List.of(100.0, 60.0, 30.0, 10.0, 5.0);
    static final List<Double> DEFAULT_QUEST_TIER_WEIGHT_PER_LEVEL = List.of(-2.0, 0.0, 1.0, 1.5, 1.5);
    /** One multiplier per Rarity. */
    static final int QUEST_TIER_COUNT = 5;

    private final double levelCurveBase;
    private final double levelCurveExponent;
    private final double accountCurveBase;
    private final double accountCurveExponent;
    private final List<Double> questTierMultipliers;
    private final List<Double> questTierMinLevels;
    private final List<Double> questTierWeightBase;
    private final List<Double> questTierWeightPerLevel;

    private RuneConfig(
            double base,
            double exponent,
            double accountBase,
            double accountExponent,
            List<Double> tiers,
            List<Double> minLevels,
            List<Double> weightBase,
            List<Double> weightPerLevel) {
        this.questTierMinLevels = List.copyOf(minLevels);
        this.questTierWeightBase = List.copyOf(weightBase);
        this.questTierWeightPerLevel = List.copyOf(weightPerLevel);
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
                DEFAULT_QUEST_TIER_MULTIPLIERS,
                DEFAULT_QUEST_TIER_MIN_LEVELS,
                DEFAULT_QUEST_TIER_WEIGHT_BASE,
                DEFAULT_QUEST_TIER_WEIGHT_PER_LEVEL);
    }

    /** Reads the config. Bad or missing values are logged as errors and replaced by defaults. */
    public static RuneConfig load(ConfigurationSection section, Logger logger) {
        double base = readPositive(section, logger, LEVEL_CURVE_BASE, DEFAULT_LEVEL_CURVE_BASE);
        double exponent = readPositive(section, logger, LEVEL_CURVE_EXPONENT, DEFAULT_LEVEL_CURVE_EXPONENT);
        double accountBase = readPositive(section, logger, ACCOUNT_CURVE_BASE, DEFAULT_ACCOUNT_CURVE_BASE);
        double accountExponent =
                readPositive(section, logger, ACCOUNT_CURVE_EXPONENT, DEFAULT_ACCOUNT_CURVE_EXPONENT);
        List<Double> tiers = readList(section, logger, QUEST_TIER_MULTIPLIERS, DEFAULT_QUEST_TIER_MULTIPLIERS, true);
        List<Double> minLevels =
                readList(section, logger, QUEST_TIER_MIN_LEVELS, DEFAULT_QUEST_TIER_MIN_LEVELS, true);
        List<Double> weightBase =
                readList(section, logger, QUEST_TIER_WEIGHT_BASE, DEFAULT_QUEST_TIER_WEIGHT_BASE, false);
        List<Double> weightPerLevel = readList(
                section, logger, QUEST_TIER_WEIGHT_PER_LEVEL, DEFAULT_QUEST_TIER_WEIGHT_PER_LEVEL, false);
        return new RuneConfig(
                base, exponent, accountBase, accountExponent, tiers, minLevels, weightBase, weightPerLevel);
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

    /** Lowest Job Level at which each Quest Tier can be drawn, lowest Tier first (Epic 15, Legendary 25). */
    public List<Double> questTierMinLevels() {
        return questTierMinLevels;
    }

    /** Quest Tier draw weight at Job Level 0, lowest Tier first. */
    public List<Double> questTierWeightBase() {
        return questTierWeightBase;
    }

    /** Quest Tier draw weight gained per Job Level (can be negative), lowest Tier first. */
    public List<Double> questTierWeightPerLevel() {
        return questTierWeightPerLevel;
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

    private static List<Double> readList(
            ConfigurationSection s, Logger log, String path, List<Double> fallback, boolean positive) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        List<?> raw = s.getList(path);
        if (raw == null || raw.size() != QUEST_TIER_COUNT) {
            log.severe("Config '" + path + "' must be a list of " + QUEST_TIER_COUNT + " numbers, got '"
                    + s.get(path) + "'. Using default " + fallback + ".");
            return fallback;
        }
        List<Double> out = new ArrayList<>();
        for (Object item : raw) {
            if (!(item instanceof Number n)
                    || !Double.isFinite(n.doubleValue())
                    || (positive && n.doubleValue() <= 0)) {
                log.severe("Config '" + path + "' must only hold "
                        + (positive ? "numbers greater than 0" : "finite numbers") + ", got '" + item
                        + "'. Using default " + fallback + ".");
                return fallback;
            }
            out.add(n.doubleValue());
        }
        return out;
    }
}
