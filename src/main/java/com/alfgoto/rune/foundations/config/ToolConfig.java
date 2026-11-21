package com.alfgoto.rune.foundations.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of Rune Tools, read from the {@code tools} section of config.yml: base speed and
 * durability by Harvest Tier, the Epic+ values, and slots and Capacity by Rarity.
 * A bad value logs a clear error and falls back to the default.
 */
public record ToolConfig(
        List<Double> speedByTier,
        double epicSpeed,
        List<Integer> durabilityByTier,
        int epicDurability,
        List<Integer> slotsByRarity,
        List<Integer> capacityByRarity,
        int pickaxeMasteryActions,
        int axeMasteryActions,
        double masteryMaxBonus,
        double traitAffinity,
        int traitGlowLight,
        double traitFierySmelt,
        double traitMountainMoveSpeed) {

    public static final String SPEED_BY_TIER = "tools.speed-by-tier";
    public static final String EPIC_SPEED = "tools.epic-speed";
    public static final String DURABILITY_BY_TIER = "tools.durability-by-tier";
    public static final String EPIC_DURABILITY = "tools.epic-durability";
    public static final String SLOTS_BY_RARITY = "tools.slots-by-rarity";
    public static final String CAPACITY_BY_RARITY = "tools.capacity-by-rarity";
    public static final String PICKAXE_MASTERY_ACTIONS = "tools.mastery.pickaxe-actions";
    public static final String AXE_MASTERY_ACTIONS = "tools.mastery.axe-actions";
    public static final String MASTERY_MAX_BONUS = "tools.mastery.max-bonus";
    public static final String TRAIT_AFFINITY = "tools.traits.affinity";
    public static final String TRAIT_GLOW_LIGHT = "tools.traits.glow-light";
    public static final String TRAIT_FIERY_SMELT = "tools.traits.fiery-smelt";
    public static final String TRAIT_MOUNTAIN_MOVE_SPEED = "tools.traits.mountain-move-speed";

    /** One value per Harvest Tier: stone, iron, diamond. */
    static final int TIER_COUNT = 3;
    /** One value per Rarity. */
    static final int RARITY_COUNT = 5;

    public ToolConfig {
        speedByTier = List.copyOf(speedByTier);
        durabilityByTier = List.copyOf(durabilityByTier);
        slotsByRarity = List.copyOf(slotsByRarity);
        capacityByRarity = List.copyOf(capacityByRarity);
    }

    /** Vanilla values (docs/design-decisions.md, Gear): stone/iron/diamond, gold speed, netherite durability. */
    public static ToolConfig defaults() {
        return new ToolConfig(
                List.of(4.0, 6.0, 8.0),
                12.0,
                List.of(131, 250, 1561),
                2031,
                List.of(1, 2, 3, 4, 5),
                List.of(3, 6, 10, 14, 18),
                500,
                1000,
                0.75,
                10,
                12,
                25,
                10);
    }

    /** Reads the {@code tools} section. Bad or missing values are logged and replaced by defaults. */
    public static ToolConfig load(ConfigurationSection section, Logger logger) {
        ToolConfig d = defaults();
        double light = readNumber(section, logger, TRAIT_GLOW_LIGHT, d.traitGlowLight);
        if (light != Math.rint(light) || light > 15) {
            logger.severe("Config '" + TRAIT_GLOW_LIGHT + "' must be a whole number from 1 to 15, got '" + light
                    + "'. Using default " + d.traitGlowLight + ".");
            light = d.traitGlowLight;
        }
        return new ToolConfig(
                readNumbers(section, logger, SPEED_BY_TIER, TIER_COUNT, d.speedByTier, false),
                readNumber(section, logger, EPIC_SPEED, d.epicSpeed),
                toInts(readNumbers(section, logger, DURABILITY_BY_TIER, TIER_COUNT, toDoubles(d.durabilityByTier), true)),
                (int) readNumber(section, logger, EPIC_DURABILITY, d.epicDurability),
                toInts(readNumbers(section, logger, SLOTS_BY_RARITY, RARITY_COUNT, toDoubles(d.slotsByRarity), true)),
                toInts(readNumbers(
                        section, logger, CAPACITY_BY_RARITY, RARITY_COUNT, toDoubles(d.capacityByRarity), true)),
                (int) readNumber(section, logger, PICKAXE_MASTERY_ACTIONS, d.pickaxeMasteryActions),
                (int) readNumber(section, logger, AXE_MASTERY_ACTIONS, d.axeMasteryActions),
                Math.min(1.0, readNumber(section, logger, MASTERY_MAX_BONUS, d.masteryMaxBonus)),
                readNumber(section, logger, TRAIT_AFFINITY, d.traitAffinity),
                (int) light,
                readNumber(section, logger, TRAIT_FIERY_SMELT, d.traitFierySmelt),
                readNumber(section, logger, TRAIT_MOUNTAIN_MOVE_SPEED, d.traitMountainMoveSpeed));
    }

    private static double readNumber(ConfigurationSection s, Logger log, String path, double fallback) {
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

    /** {@code whole}: the numbers must be whole (durability, slots, Capacity). Speeds may have decimals. */
    private static List<Double> readNumbers(
            ConfigurationSection s, Logger log, String path, int size, List<Double> fallback, boolean whole) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        List<?> raw = s.getList(path);
        if (raw == null || raw.size() != size) {
            log.severe("Config '" + path + "' must be a list of " + size + " numbers, got '" + s.get(path)
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        List<Double> out = new ArrayList<>();
        for (Object item : raw) {
            if (!(item instanceof Number n)
                    || !Double.isFinite(n.doubleValue())
                    || n.doubleValue() <= 0
                    || (whole && n.doubleValue() != Math.rint(n.doubleValue()))) {
                log.severe("Config '" + path + "' must only hold " + (whole ? "whole " : "")
                        + "numbers greater than 0, got '" + item + "'. Using default " + fallback + ".");
                return fallback;
            }
            out.add(n.doubleValue());
        }
        return out;
    }

    private static List<Double> toDoubles(List<Integer> values) {
        return values.stream().map(Integer::doubleValue).toList();
    }

    private static List<Integer> toInts(List<Double> values) {
        return values.stream().map(Double::intValue).toList();
    }
}
