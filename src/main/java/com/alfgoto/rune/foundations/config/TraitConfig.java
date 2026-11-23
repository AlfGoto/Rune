package com.alfgoto.rune.foundations.config;

import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of the Copper and Momentum Traits, read from {@code tools.copper} and
 * {@code tools.momentum} in config.yml. A bad value logs a clear error and falls back to the default.
 *
 * @param copperBlocksPerStage blocks broken for one oxidation stage (4 stages, so 3 steps)
 * @param copperSpeed haste in % at the last stage (the Trait value, scaled by stage / 3)
 * @param momentumSpeed haste in % at full Momentum (the Trait value, scaled by blocks in a row / max)
 * @param momentumMaxBlocks blocks in a row at full Momentum
 * @param momentumResetSeconds seconds between two breaks that reset Momentum
 * @param momentumCapacityModifier Capacity added to the Redstone Pickaxe and Breeze Axe (negative: a drawback)
 */
public record TraitConfig(
        int copperBlocksPerStage,
        double copperSpeed,
        double momentumSpeed,
        int momentumMaxBlocks,
        double momentumResetSeconds,
        int momentumCapacityModifier) {

    public static final String COPPER_BLOCKS_PER_STAGE = "tools.copper.blocks-per-stage";
    public static final String COPPER_SPEED = "tools.copper.speed";
    public static final String MOMENTUM_SPEED = "tools.momentum.speed";
    public static final String MOMENTUM_MAX_BLOCKS = "tools.momentum.max-blocks";
    public static final String MOMENTUM_RESET_SECONDS = "tools.momentum.reset-seconds";
    public static final String MOMENTUM_CAPACITY_MODIFIER = "tools.momentum.capacity-modifier";

    /** Starting values decided by Alfred (2026-11-16, #72): 300 blocks, +30%; +50% at 10 blocks, 2 s. */
    public static TraitConfig defaults() {
        return new TraitConfig(300, 30, 50, 10, 2, -2);
    }

    public static TraitConfig load(ConfigurationSection section, Logger logger) {
        TraitConfig d = defaults();
        return new TraitConfig(
                (int) whole(section, logger, COPPER_BLOCKS_PER_STAGE, d.copperBlocksPerStage),
                number(section, logger, COPPER_SPEED, d.copperSpeed),
                number(section, logger, MOMENTUM_SPEED, d.momentumSpeed),
                (int) whole(section, logger, MOMENTUM_MAX_BLOCKS, d.momentumMaxBlocks),
                number(section, logger, MOMENTUM_RESET_SECONDS, d.momentumResetSeconds),
                capacity(section, logger, MOMENTUM_CAPACITY_MODIFIER, d.momentumCapacityModifier));
    }

    private static int capacity(ConfigurationSection s, Logger log, String path, int fallback) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        Object raw = s.get(path);
        if (!(raw instanceof Number n) || n.doubleValue() != Math.rint(n.doubleValue())
                || Math.abs(n.doubleValue()) > 1000) {
            log.severe("Config '" + path + "' must be a whole number from -1000 to 1000, got '" + raw
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        return n.intValue();
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
        if (value != Math.rint(value) || value > 1_000_000) {
            log.severe("Config '" + path + "' must be a whole number from 1 to 1000000, got '" + value
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        return value;
    }
}
