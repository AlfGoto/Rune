package com.alfgoto.rune.foundations.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Balancing numbers of Rune Armor, read from the {@code armor} section of config.yml: protection
 * and durability by piece and Rarity (Common is leather, Uncommon chainmail, Rare iron, Epic diamond,
 * Legendary netherite), and toughness by Rarity. Slots and Capacity are the
 * ones of {@code tools} (same table for Rune Tools and Rune Armor).
 * A bad value logs a clear error and falls back to the default.
 *
 * <p>Pieces are keyed by lower case name: {@code helmet}, {@code chestplate}, {@code leggings}, {@code boots}.
 */
public record ArmorConfig(
        Map<String, List<Integer>> defenseByRarity,
        Map<String, List<Integer>> durabilityByRarity,
        List<Double> toughnessByRarity,
        int depthTopY,
        int depthBottomY) {

    public static final String DEPTH_TOP_Y = "armor.depth.top-y";
    public static final String DEPTH_BOTTOM_Y = "armor.depth.bottom-y";
    public static final String DEFENSE = "armor.defense";
    public static final String DURABILITY = "armor.durability";
    public static final String TOUGHNESS = "armor.toughness-by-rarity";

    static final List<String> PIECES = List.of("helmet", "chestplate", "leggings", "boots");
    static final int RARITY_COUNT = 5;

    public ArmorConfig {
        defenseByRarity = Map.copyOf(defenseByRarity);
        durabilityByRarity = Map.copyOf(durabilityByRarity);
        toughnessByRarity = List.copyOf(toughnessByRarity);
        if (depthTopY <= depthBottomY) {
            throw new IllegalArgumentException(
                    "Depth top Y (" + depthTopY + ") must be above bottom Y (" + depthBottomY + ")");
        }
    }

    /** Vanilla values: leather, chainmail, iron, diamond, netherite (docs/design-decisions.md, Tools & armor). */
    public static ArmorConfig defaults() {
        return new ArmorConfig(
                Map.of(
                        "helmet", List.of(1, 2, 2, 3, 3),
                        "chestplate", List.of(3, 5, 6, 8, 8),
                        "leggings", List.of(2, 4, 5, 6, 6),
                        "boots", List.of(1, 1, 2, 3, 3)),
                Map.of(
                        "helmet", List.of(55, 165, 165, 363, 407),
                        "chestplate", List.of(80, 240, 240, 528, 592),
                        "leggings", List.of(75, 225, 225, 495, 555),
                        "boots", List.of(65, 195, 195, 429, 481)),
                List.of(0.0, 0.0, 0.0, 2.0, 3.0),
                64,
                -64);
    }

    /** Reads the {@code armor} section. Bad or missing values are logged and replaced by defaults. */
    public static ArmorConfig load(ConfigurationSection section, Logger logger) {
        ArmorConfig d = defaults();
        int top = readY(section, logger, DEPTH_TOP_Y, d.depthTopY);
        int bottom = readY(section, logger, DEPTH_BOTTOM_Y, d.depthBottomY);
        if (top <= bottom) {
            logger.severe("Config '" + DEPTH_TOP_Y + "' (" + top + ") must be above '" + DEPTH_BOTTOM_Y + "' ("
                    + bottom + "). Using defaults " + d.depthTopY + " and " + d.depthBottomY + ".");
            top = d.depthTopY;
            bottom = d.depthBottomY;
        }
        return new ArmorConfig(
                readPieces(section, logger, DEFENSE, d.defenseByRarity),
                readPieces(section, logger, DURABILITY, d.durabilityByRarity),
                readList(section, logger, TOUGHNESS, d.toughnessByRarity),
                top,
                bottom);
    }

    private static int readY(ConfigurationSection s, Logger log, String path, int fallback) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        if (!(s.get(path) instanceof Number n) || n.doubleValue() != Math.rint(n.doubleValue())) {
            log.severe("Config '" + path + "' must be a whole number, got '" + s.get(path) + "'. Using default "
                    + fallback + ".");
            return fallback;
        }
        return n.intValue();
    }

    private static Map<String, List<Integer>> readPieces(
            ConfigurationSection s, Logger log, String path, Map<String, List<Integer>> fallback) {
        Map<String, List<Integer>> out = new LinkedHashMap<>();
        for (String piece : PIECES) {
            String key = path + "." + piece;
            List<Double> values = readList(s, log, key, toDoubles(fallback.get(piece)));
            out.put(piece, values.stream().map(v -> (int) Math.rint(v)).toList());
        }
        return out;
    }

    /** Zero is allowed (no toughness). */
    private static List<Double> readList(ConfigurationSection s, Logger log, String path, List<Double> fallback) {
        if (!s.contains(path)) {
            log.severe("Config '" + path + "' is missing. Using default " + fallback + ".");
            return fallback;
        }
        List<?> raw = s.getList(path);
        if (raw == null || raw.size() != RARITY_COUNT) {
            log.severe("Config '" + path + "' must be a list of " + RARITY_COUNT + " numbers, got '" + s.get(path)
                    + "'. Using default " + fallback + ".");
            return fallback;
        }
        boolean whole = path.startsWith(DEFENSE) || path.startsWith(DURABILITY);
        List<Double> out = new java.util.ArrayList<>();
        for (Object item : raw) {
            if (!(item instanceof Number n)
                    || !Double.isFinite(n.doubleValue())
                    || n.doubleValue() < 0
                    || (whole && n.doubleValue() != Math.rint(n.doubleValue()))
                    || (path.startsWith(DURABILITY) && n.doubleValue() < 1)) {
                log.severe("Config '" + path + "' must only hold " + (whole ? "whole " : "") + "numbers from "
                        + (path.startsWith(DURABILITY) ? "1" : "0") + " up" + ", got '" + item
                        + "'. Using default " + fallback + ".");
                return fallback;
            }
            out.add(n.doubleValue());
        }
        return out;
    }

    private static List<Double> toDoubles(List<Integer> values) {
        return values.stream().map(Integer::doubleValue).toList();
    }
}
