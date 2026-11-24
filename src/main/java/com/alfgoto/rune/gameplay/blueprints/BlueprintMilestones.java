package com.alfgoto.rune.gameplay.blueprints;

import com.alfgoto.rune.foundations.config.BlueprintConfig;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobLevelListener;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Gives the Milestone Blueprints (config {@code blueprints.milestones}) when a Job Level goes up. A Blueprint the
 * player already used is not given again; one that is not registered yet is skipped.
 */
final class BlueprintMilestones implements JobLevelListener {

    private final Blueprints blueprints;
    private final BlueprintConfig config;

    BlueprintMilestones(Blueprints blueprints, BlueprintConfig config) {
        this.blueprints = blueprints;
        this.config = config;
    }

    @Override
    public void onJobLevelChange(UUID uuid, Job job, int levelBefore, int levelAfter) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        for (BlueprintConfig.Milestone milestone : config.milestones()) {
            if (!milestone.job().equals(job.id())
                    || milestone.level() <= levelBefore
                    || milestone.level() > levelAfter) {
                continue;
            }
            Blueprint blueprint = blueprints.find(milestone.blueprint()).orElse(null);
            if (blueprint == null || blueprints.isUnlocked(uuid, blueprint.id())) {
                continue;
            }
            GiveSubcommand.give(player, blueprints.create(blueprint));
            player.sendMessage(Component.translatable(
                            "rune.blueprint.milestone",
                            "Level %s reached: Blueprint earned: %s.",
                            Component.text(milestone.level()),
                            Component.translatable(blueprint.translationKey(), blueprint.fallbackName()))
                    .color(NamedTextColor.GOLD));
        }
    }
}
