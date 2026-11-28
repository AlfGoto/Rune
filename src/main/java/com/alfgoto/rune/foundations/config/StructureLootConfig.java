package com.alfgoto.rune.foundations.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Structure loot, read from the {@code structure-loot} list of config.yml (docs/design-decisions.md, Relics):
 * one entry per item injected into the chests of one loot table, with its odds and its Job Level gate.
 * Items and Jobs are kept as ids (Foundations does not know them). A bad entry logs an error and is skipped.
 *
 * @param entries one entry per injected item
 */
public record StructureLootConfig(List<Entry> entries) {

    public static final String PATH = "structure-loot";

    /**
     * One injected item.
     *
     * @param table loot table key, e.g. {@code minecraft:chests/ancient_city}
     * @param blueprint true when {@code id} is a Blueprint id, false when it is a Rune item id
     * @param id the Rune item or Blueprint id
     * @param chance chance per generated chest, from 0 to 1
     * @param job the Job whose level gates it
     * @param minLevel the Job Level the player needs
     */
    public record Entry(String table, boolean blueprint, String id, double chance, String job, int minLevel) {}

    public StructureLootConfig {
        entries = List.copyOf(entries);
    }

    public static StructureLootConfig defaults() {
        return new StructureLootConfig(List.of(
                new Entry("minecraft:chests/ancient_city", false, "warden_pickaxe", 0.01, "mining", 25),
                new Entry("minecraft:chests/ancient_city", true, "crown_of_the_depths", 0.03, "mining", 20),
                new Entry("minecraft:chests/woodland_mansion", false, "timberfall", 0.01, "woodcutting", 25),
                new Entry("minecraft:chests/woodland_mansion", false, "evokers_hatchet", 0.01, "woodcutting", 25),
                new Entry("minecraft:chests/woodland_mansion", true, "stags_hooves", 0.03, "woodcutting", 20),
                new Entry("minecraft:chests/buried_treasure", false, "motherroot", 0.005, "woodcutting", 25),
                new Entry("rune:test_chest", false, "test_item", 1.0, "mining", 25),
                new Entry("rune:test_chest", true, "test_blueprint", 1.0, "mining", 25)));
    }

    /** Reads the {@code structure-loot} list. A missing list means the documented defaults. */
    public static StructureLootConfig load(ConfigurationSection section, Logger logger) {
        if (!section.contains(PATH)) {
            return defaults();
        }
        List<Entry> out = new ArrayList<>();
        int index = 0;
        for (var raw : section.getMapList(PATH)) {
            index++;
            String table = text(raw.get("table"));
            String item = text(raw.get("item"));
            String blueprint = text(raw.get("blueprint"));
            String job = text(raw.get("job"));
            Double chance = number(raw.get("chance"));
            Double level = number(raw.get("min-level"));
            boolean oneItem = item.isEmpty() != blueprint.isEmpty();
            if (table.isEmpty()
                    || !oneItem
                    || job.isEmpty()
                    || chance == null
                    || chance < 0
                    || chance > 1
                    || level == null
                    || level < 1) {
                logger.severe("Config '" + PATH + "' entry " + index + " needs 'table', one of 'item' or"
                        + " 'blueprint', 'job', a 'chance' from 0 to 1 and a 'min-level' of 1 or more. Entry skipped.");
                continue;
            }
            boolean isBlueprint = !blueprint.isEmpty();
            out.add(new Entry(
                    table.contains(":") ? table : "minecraft:" + table,
                    isBlueprint,
                    isBlueprint ? blueprint : item,
                    chance,
                    job,
                    level.intValue()));
        }
        return new StructureLootConfig(out);
    }

    private static String text(Object raw) {
        return raw == null ? "" : raw.toString().trim().toLowerCase(Locale.ROOT);
    }

    private static Double number(Object raw) {
        return raw instanceof Number number ? number.doubleValue() : null;
    }
}
