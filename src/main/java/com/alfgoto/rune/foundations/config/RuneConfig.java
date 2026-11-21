package com.alfgoto.rune.foundations.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.logging.Logger;
import org.bukkit.Material;
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
    public static final String BONEMEAL_XP_MULTIPLIER = "jobs.bonemeal-multiplier";
    public static final String BLOCK_XP = "jobs.block-xp";
    public static final String QUEST_TIER_MULTIPLIERS = "quests.tier-multipliers";
    public static final String QUEST_TIER_MIN_LEVELS = "quests.tier-min-levels";
    public static final String QUEST_TIER_WEIGHT_BASE = "quests.tier-weights.base";
    public static final String QUEST_TIER_WEIGHT_PER_LEVEL = "quests.tier-weights.per-level";
    public static final String QUEST_MAX_REROLL_TOKENS = "quests.max-reroll-tokens";
    public static final String QUEST_REWARD_JOB_XP = "quests.reward-job-xp";
    public static final String QUEST_REWARD_ACCOUNT_XP = "quests.reward-account-xp";
    public static final String RUNE_COST_BASE = "capacity.rune-cost.base";
    public static final String RANK_UP_COST_MULTIPLIER = "runes.rank-up.cost-multiplier";
    public static final double DEFAULT_RANK_UP_COST_MULTIPLIER = 3;
    public static final String RUNE_COST_PER_RANK = "capacity.rune-cost.per-rank";
    public static final String PLAYER_CAPACITY_BASE = "capacity.player.base";
    public static final String PLAYER_CAPACITY_PER_LEVEL = "capacity.player.per-account-level";
    public static final String PLAYER_CAPACITY_MAX = "capacity.player.max";
    public static final String REWARDS_RARITY_BOOST = "rewards.rarity-boost";
    public static final String REWARDS_ROLLS_BY_RARITY = "rewards.rolls-by-rarity";
    public static final String REWARDS_JOB_BIAS = "rewards.job-bias";

    static final double DEFAULT_LEVEL_CURVE_BASE = 50;
    static final double DEFAULT_LEVEL_CURVE_EXPONENT = 1.3;
    static final double DEFAULT_ACCOUNT_CURVE_BASE = 200;
    static final double DEFAULT_ACCOUNT_CURVE_EXPONENT = 1.3;
    static final double DEFAULT_BONEMEAL_XP_MULTIPLIER = 0.25;
    static final List<Double> DEFAULT_QUEST_TIER_MULTIPLIERS = List.of(1.0, 1.5, 2.0, 3.0, 5.0);
    static final List<Double> DEFAULT_QUEST_TIER_MIN_LEVELS = List.of(1.0, 1.0, 1.0, 15.0, 25.0);
    static final List<Double> DEFAULT_QUEST_TIER_WEIGHT_BASE = List.of(100.0, 60.0, 30.0, 10.0, 5.0);
    static final List<Double> DEFAULT_QUEST_TIER_WEIGHT_PER_LEVEL = List.of(-1.2, 0.0, 0.6, 0.5, 0.2);
    static final int DEFAULT_QUEST_MAX_REROLL_TOKENS = 3;
    static final List<Double> DEFAULT_QUEST_REWARD_JOB_XP = List.of(50.0, 75.0, 100.0, 150.0, 250.0);
    static final List<Double> DEFAULT_QUEST_REWARD_ACCOUNT_XP = List.of(25.0, 40.0, 50.0, 75.0, 125.0);
    static final List<Double> DEFAULT_RUNE_COST_BASE = List.of(1.0, 2.0, 3.0, 5.0, 8.0);
    static final double DEFAULT_RUNE_COST_PER_RANK = 1;
    static final int DEFAULT_PLAYER_CAPACITY_BASE = 5;
    static final int DEFAULT_PLAYER_CAPACITY_PER_LEVEL = 1;
    static final int DEFAULT_PLAYER_CAPACITY_MAX = 40;
    static final List<Double> DEFAULT_REWARDS_RARITY_BOOST = List.of(1.0, 1.5, 2.0, 3.0, 4.0);
    static final List<Double> DEFAULT_REWARDS_ROLLS_BY_RARITY = List.of(1.0, 1.0, 2.0, 2.0, 3.0);
    static final double DEFAULT_REWARDS_JOB_BIAS = 0.6;
    /** One multiplier per Rarity. */
    static final int QUEST_TIER_COUNT = 5;

    private final double levelCurveBase;
    private final double levelCurveExponent;
    private final double accountCurveBase;
    private final double accountCurveExponent;
    private final double bonemealXpMultiplier;
    private final Map<Material, Double> blockXp;
    private final List<Double> questTierMultipliers;
    private final List<Double> questTierMinLevels;
    private final List<Double> questTierWeightBase;
    private final List<Double> questTierWeightPerLevel;
    private final int questMaxRerollTokens;
    private final List<Double> questRewardJobXp;
    private final List<Double> questRewardAccountXp;
    private final List<Double> runeCostBase;
    private final double runeCostPerRank;
    private final int playerCapacityBase;
    private final int playerCapacityPerLevel;
    private final int playerCapacityMax;
    private final List<Double> rewardsRarityBoost;
    private final List<Integer> rewardsRollsByRarity;
    private final double rewardsJobBias;
    private final ToolConfig tools;
    private final ArmorConfig armor;
    private final SpeedRuneConfig speedRunes;
    private CacheConfig caches = CacheConfig.defaults();
    private ConfigurationSection runes; // the 'runes' section, null when there is none
    private double rankUpCostMultiplier = DEFAULT_RANK_UP_COST_MULTIPLIER;

    private RuneConfig(
            double base,
            double exponent,
            double accountBase,
            double accountExponent,
            double bonemealXpMultiplier,
            Map<Material, Double> blockXp,
            List<Double> tiers,
            List<Double> minLevels,
            List<Double> weightBase,
            List<Double> weightPerLevel,
            int maxRerollTokens,
            List<Double> questRewardJobXp,
            List<Double> questRewardAccountXp,
            List<Double> runeCostBase,
            double runeCostPerRank,
            int playerCapacityBase,
            int playerCapacityPerLevel,
            int playerCapacityMax,
            List<Double> rewardsRarityBoost,
            List<Double> rewardsRollsByRarity,
            double rewardsJobBias,
            ToolConfig tools,
            ArmorConfig armor,
            SpeedRuneConfig speedRunes) {
        this.rewardsRarityBoost = List.copyOf(rewardsRarityBoost);
        this.rewardsRollsByRarity = rewardsRollsByRarity.stream()
                .map(rolls -> (int) Math.min(1000, Math.max(1, Math.round(rolls))))
                .toList();
        this.rewardsJobBias = rewardsJobBias;
        this.playerCapacityBase = playerCapacityBase;
        this.playerCapacityPerLevel = playerCapacityPerLevel;
        this.playerCapacityMax = playerCapacityMax;
        this.runeCostBase = List.copyOf(runeCostBase);
        this.runeCostPerRank = runeCostPerRank;
        this.tools = tools;
        this.armor = armor;
        this.speedRunes = speedRunes;
        this.questMaxRerollTokens = maxRerollTokens;
        this.questRewardJobXp = List.copyOf(questRewardJobXp);
        this.questRewardAccountXp = List.copyOf(questRewardAccountXp);
        this.questTierMinLevels = List.copyOf(minLevels);
        this.questTierWeightBase = List.copyOf(weightBase);
        this.questTierWeightPerLevel = List.copyOf(weightPerLevel);
        this.levelCurveBase = base;
        this.levelCurveExponent = exponent;
        this.accountCurveBase = accountBase;
        this.accountCurveExponent = accountExponent;
        this.bonemealXpMultiplier = bonemealXpMultiplier;
        this.blockXp = Map.copyOf(blockXp);
        this.questTierMultipliers = List.copyOf(tiers);
    }

    /** All defaults, no file needed. */
    public static RuneConfig defaults() {
        return new RuneConfig(
                DEFAULT_LEVEL_CURVE_BASE,
                DEFAULT_LEVEL_CURVE_EXPONENT,
                DEFAULT_ACCOUNT_CURVE_BASE,
                DEFAULT_ACCOUNT_CURVE_EXPONENT,
                DEFAULT_BONEMEAL_XP_MULTIPLIER,
                Map.of(),
                DEFAULT_QUEST_TIER_MULTIPLIERS,
                DEFAULT_QUEST_TIER_MIN_LEVELS,
                DEFAULT_QUEST_TIER_WEIGHT_BASE,
                DEFAULT_QUEST_TIER_WEIGHT_PER_LEVEL,
                DEFAULT_QUEST_MAX_REROLL_TOKENS,
                DEFAULT_QUEST_REWARD_JOB_XP,
                DEFAULT_QUEST_REWARD_ACCOUNT_XP,
                DEFAULT_RUNE_COST_BASE,
                DEFAULT_RUNE_COST_PER_RANK,
                DEFAULT_PLAYER_CAPACITY_BASE,
                DEFAULT_PLAYER_CAPACITY_PER_LEVEL,
                DEFAULT_PLAYER_CAPACITY_MAX,
                DEFAULT_REWARDS_RARITY_BOOST,
                DEFAULT_REWARDS_ROLLS_BY_RARITY,
                DEFAULT_REWARDS_JOB_BIAS,
                ToolConfig.defaults(),
                ArmorConfig.defaults(),
                SpeedRuneConfig.defaults());
    }

    /** Reads the config. Bad or missing values are logged as errors and replaced by defaults. */
    public static RuneConfig load(ConfigurationSection section, Logger logger) {
        double base = readPositive(section, logger, LEVEL_CURVE_BASE, DEFAULT_LEVEL_CURVE_BASE);
        double exponent = readPositive(section, logger, LEVEL_CURVE_EXPONENT, DEFAULT_LEVEL_CURVE_EXPONENT);
        double accountBase = readPositive(section, logger, ACCOUNT_CURVE_BASE, DEFAULT_ACCOUNT_CURVE_BASE);
        double accountExponent =
                readPositive(section, logger, ACCOUNT_CURVE_EXPONENT, DEFAULT_ACCOUNT_CURVE_EXPONENT);
        double bonemeal = readPositive(section, logger, BONEMEAL_XP_MULTIPLIER, DEFAULT_BONEMEAL_XP_MULTIPLIER);
        Map<Material, Double> blockXp = readBlockXp(section, logger);
        List<Double> tiers = readList(section, logger, QUEST_TIER_MULTIPLIERS, DEFAULT_QUEST_TIER_MULTIPLIERS, true);
        List<Double> minLevels =
                readList(section, logger, QUEST_TIER_MIN_LEVELS, DEFAULT_QUEST_TIER_MIN_LEVELS, true);
        List<Double> weightBase =
                readList(section, logger, QUEST_TIER_WEIGHT_BASE, DEFAULT_QUEST_TIER_WEIGHT_BASE, false);
        List<Double> weightPerLevel = readList(
                section, logger, QUEST_TIER_WEIGHT_PER_LEVEL, DEFAULT_QUEST_TIER_WEIGHT_PER_LEVEL, false);
        int maxTokens = (int) Math.min(
                1_000_000,
                readPositive(section, logger, QUEST_MAX_REROLL_TOKENS, DEFAULT_QUEST_MAX_REROLL_TOKENS));
        List<Double> rewardJobXp = readList(section, logger, QUEST_REWARD_JOB_XP, DEFAULT_QUEST_REWARD_JOB_XP, true);
        List<Double> rewardAccountXp =
                readList(section, logger, QUEST_REWARD_ACCOUNT_XP, DEFAULT_QUEST_REWARD_ACCOUNT_XP, true);
        List<Double> costBase = readList(section, logger, RUNE_COST_BASE, DEFAULT_RUNE_COST_BASE, true);
        double costPerRank = readPositive(section, logger, RUNE_COST_PER_RANK, DEFAULT_RUNE_COST_PER_RANK);
        int capacityBase = readCount(section, logger, PLAYER_CAPACITY_BASE, DEFAULT_PLAYER_CAPACITY_BASE);
        int capacityPerLevel =
                readCount(section, logger, PLAYER_CAPACITY_PER_LEVEL, DEFAULT_PLAYER_CAPACITY_PER_LEVEL);
        int capacityMax = readCount(section, logger, PLAYER_CAPACITY_MAX, DEFAULT_PLAYER_CAPACITY_MAX);
        List<Double> rarityBoost =
                readList(section, logger, REWARDS_RARITY_BOOST, DEFAULT_REWARDS_RARITY_BOOST, true);
        List<Double> rollsByRarity =
                readList(section, logger, REWARDS_ROLLS_BY_RARITY, DEFAULT_REWARDS_ROLLS_BY_RARITY, true);
        double jobBias = readPositive(section, logger, REWARDS_JOB_BIAS, DEFAULT_REWARDS_JOB_BIAS);
        if (jobBias > 1) {
            logger.severe("Config '" + REWARDS_JOB_BIAS + "' must be a chance from 0 to 1, got " + jobBias
                    + ". Using default " + DEFAULT_REWARDS_JOB_BIAS + ".");
            jobBias = DEFAULT_REWARDS_JOB_BIAS;
        }
        RuneConfig loaded = new RuneConfig(
                base,
                exponent,
                accountBase,
                accountExponent,
                bonemeal,
                blockXp,
                tiers,
                minLevels,
                weightBase,
                weightPerLevel,
                maxTokens,
                rewardJobXp,
                rewardAccountXp,
                costBase,
                costPerRank,
                capacityBase,
                capacityPerLevel,
                capacityMax,
                rarityBoost,
                rollsByRarity,
                jobBias,
                ToolConfig.load(section, logger),
                ArmorConfig.load(section, logger),
                SpeedRuneConfig.load(section, logger));
        loaded.runes = section.getConfigurationSection("runes");
        loaded.caches = CacheConfig.load(section, logger);
        loaded.rankUpCostMultiplier =
                readPositive(section, logger, RANK_UP_COST_MULTIPLIER, DEFAULT_RANK_UP_COST_MULTIPLIER);
        return loaded;
    }

    /** Rank-up cost in vanilla XP levels: going up from Rank N costs this number to the power N (3: 3, 9, 27…). */
    public double rankUpCostMultiplier() {
        return rankUpCostMultiplier;
    }

    /** Rune Cache drop chance and Milestone Cache table. */
    public CacheConfig caches() {
        return caches;
    }

    /** Mining speed Runes balancing: Haste, Specialist and Affinity. */
    public SpeedRuneConfig speedRunes() {
        return speedRunes;
    }

    /**
     * A number of a Rune from {@code runes.<runeId>.<key>} (e.g. {@code runes.fortune.base}). Empty when the
     * value is missing or not a finite number.
     */
    public OptionalDouble runeNumber(String runeId, String key) {
        if (runes == null
                || !(runes.get(runeId + "." + key) instanceof Number n)
                || !Double.isFinite(n.doubleValue())) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(n.doubleValue());
    }

    /** Rune Tool balancing: speed, durability, slots and Capacity. */
    public ToolConfig tools() {
        return tools;
    }

    /** Rune Armor balancing: protection, durability, toughness. */
    public ArmorConfig armor() {
        return armor;
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

    /** Job XP multiplier for blocks grown with bone meal (0.25). */
    public double bonemealXpMultiplier() {
        return bonemealXpMultiplier;
    }

    /** Base Job XP per block broken. A block that is not listed gives no XP. */
    public Map<Material, Double> blockXp() {
        return blockXp;
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

    /** Capacity Cost of a Rank 1 Rune, one per Rarity, lowest first (1, 2, 3, 5, 8). */
    public List<Double> runeCostBase() {
        return runeCostBase;
    }

    /** Capacity Cost added for each Rank above 1 (1). */
    public double runeCostPerRank() {
        return runeCostPerRank;
    }

    /** Player Capacity at Account Level 0 (5). */
    public int playerCapacityBase() {
        return playerCapacityBase;
    }

    /** Player Capacity gained per Account Level (1). */
    public int playerCapacityPerLevel() {
        return playerCapacityPerLevel;
    }

    /** Highest Player Capacity (40). */
    public int playerCapacityMax() {
        return playerCapacityMax;
    }

    /** Odds boost per source Rarity, lowest first: an entry weighs base x boost ^ (its Rarity index). */
    public List<Double> rewardsRarityBoost() {
        return rewardsRarityBoost;
    }

    /** How many times a source of each Rarity rolls a loot table, lowest first. */
    public List<Integer> rewardsRollsByRarity() {
        return rewardsRollsByRarity;
    }

    /** Chance that a Job-bound Rune roll picks that Job's Runes (0.6). */
    public double rewardsJobBias() {
        return rewardsJobBias;
    }

    /** Most Reroll Tokens a player can hold. */
    public int questMaxRerollTokens() {
        return questMaxRerollTokens;
    }

    /** Job XP a completed Quest gives, lowest Tier first. */
    public List<Double> questRewardJobXp() {
        return questRewardJobXp;
    }

    /** Account XP bonus a completed Quest gives, lowest Tier first. */
    public List<Double> questRewardAccountXp() {
        return questRewardAccountXp;
    }

    /** Quest Tier draw weight gained per Job Level (can be negative), lowest Tier first. */
    public List<Double> questTierWeightPerLevel() {
        return questTierWeightPerLevel;
    }

    private static Map<Material, Double> readBlockXp(ConfigurationSection s, Logger log) {
        ConfigurationSection section = s.getConfigurationSection(BLOCK_XP);
        if (section == null) {
            log.severe("Config '" + BLOCK_XP + "' is missing. No block gives Job XP.");
            return Map.of();
        }
        Map<Material, Double> out = new LinkedHashMap<>();
        for (String name : section.getKeys(false)) {
            Material material = Material.getMaterial(name);
            if (material == null) {
                log.severe("Config '" + BLOCK_XP + "." + name + "' is not a block name. Entry skipped.");
                continue;
            }
            Object raw = section.get(name);
            if (!(raw instanceof Number n) || !Double.isFinite(n.doubleValue()) || n.doubleValue() <= 0) {
                log.severe("Config '" + BLOCK_XP + "." + name + "' must be a number greater than 0, got '" + raw
                        + "'. Entry skipped.");
                continue;
            }
            out.put(material, n.doubleValue());
        }
        return out;
    }

    /** A whole number from 0 to 1,000,000. */
    private static int readCount(ConfigurationSection s, Logger log, String path, int fallback) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        Object raw = s.get(path);
        if (!(raw instanceof Number n)
                || n.doubleValue() != Math.rint(n.doubleValue())
                || n.doubleValue() < 0
                || n.doubleValue() > 1_000_000) {
            log.severe("Config '" + path + "' must be a whole number from 0 to 1000000, got '" + raw
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        return n.intValue();
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
