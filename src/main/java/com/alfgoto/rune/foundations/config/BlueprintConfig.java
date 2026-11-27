package com.alfgoto.rune.foundations.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Milestone Blueprints, read from the {@code blueprints.milestones} section of config.yml
 * (docs/jobs/mining.md, docs/jobs/woodcutting.md): the Blueprint a player gets at a Job Level.
 * Jobs and Blueprints are kept as ids (Foundations does not know them). A bad entry logs an error and is skipped.
 *
 * @param milestones one entry per guaranteed Blueprint
 */
public record BlueprintConfig(List<Milestone> milestones) {

    public static final String MILESTONES = "blueprints.milestones";

    /** The Job Level of {@code job} at which the Blueprint {@code blueprint} is given. */
    public record Milestone(String job, int level, String blueprint) {}

    public BlueprintConfig {
        milestones = List.copyOf(milestones);
    }

    public static BlueprintConfig defaults() {
        return new BlueprintConfig(List.of(
                new Milestone("mining", 22, "briseroc"),
                new Milestone("woodcutting", 22, "elder_axe"),
                new Milestone("mining", 30, "heart_of_the_mountain"),
                new Milestone("woodcutting", 30, "elders_bark")));
    }

    /** Reads {@code blueprints.milestones}: Job id, then Job Level, then Blueprint id. */
    public static BlueprintConfig load(ConfigurationSection section, Logger logger) {
        ConfigurationSection jobs = section.getConfigurationSection(MILESTONES);
        if (jobs == null) {
            return defaults(); // an older config.yml has no section: the documented milestones apply
        }
        List<Milestone> out = new ArrayList<>();
        for (String job : jobs.getKeys(false)) {
            ConfigurationSection levels = jobs.getConfigurationSection(job);
            if (levels == null) {
                logger.severe("Config '" + MILESTONES + "." + job + "' must list Job Levels. Entry skipped.");
                continue;
            }
            for (String key : levels.getKeys(false)) {
                Object raw = levels.get(key);
                int level;
                try {
                    level = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    level = 0;
                }
                String blueprint = raw == null ? "" : raw.toString().trim().toLowerCase(java.util.Locale.ROOT);
                if (level < 1 || blueprint.isEmpty()) {
                    logger.severe("Config '" + MILESTONES + "." + job + "." + key
                            + "' must be a Job Level and a Blueprint id, got '" + raw + "'. Entry skipped.");
                    continue;
                }
                out.add(new Milestone(job.toLowerCase(java.util.Locale.ROOT), level, blueprint));
            }
        }
        return new BlueprintConfig(out);
    }
}
