package com.alfgoto.rune.foundations.config;

import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of the biome pickaxe Traits (Lichen, Fiery, Emerald, Prismarine), read from the
 * {@code tools.traits} section of config.yml. A bad value logs a clear error and falls back to the default.
 *
 * @param affinity % mining speed of an Affinity Trait in its Place
 * @param glowLight light level (1 to 15) of the Lichen glow
 * @param fierySmelt Smelt chance in % of the Fiery Trait
 * @param mountainMoveSpeed % move speed of the Emerald Trait in Mountains
 */
public record TraitConfig(double affinity, int glowLight, double fierySmelt, double mountainMoveSpeed) {

    public static final String AFFINITY = "tools.traits.affinity";
    public static final String GLOW_LIGHT = "tools.traits.glow-light";
    public static final String FIERY_SMELT = "tools.traits.fiery-smelt";
    public static final String MOUNTAIN_MOVE_SPEED = "tools.traits.mountain-move-speed";

    public static TraitConfig defaults() {
        return new TraitConfig(10, 12, 25, 10);
    }

    public static TraitConfig load(ConfigurationSection section, Logger logger) {
        TraitConfig d = defaults();
        double light = number(section, logger, GLOW_LIGHT, d.glowLight);
        if (light != Math.rint(light) || light > 15) {
            logger.severe("Config '" + GLOW_LIGHT + "' must be a whole number from 1 to 15, got '" + light
                    + "'. Using default " + d.glowLight + ".");
            light = d.glowLight;
        }
        return new TraitConfig(
                number(section, logger, AFFINITY, d.affinity),
                (int) light,
                number(section, logger, FIERY_SMELT, d.fierySmelt),
                number(section, logger, MOUNTAIN_MOVE_SPEED, d.mountainMoveSpeed));
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
}
