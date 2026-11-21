package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.jobs.JobLevelListener;
import com.alfgoto.rune.text.Rarity;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Gives Caches for Jobs: a chance of a Cache at the block on every Job Action (Cache Rarity: Common for now),
 * and the Milestone Cache when a Job Level goes up. Both Caches are for the Job that earned them.
 */
final class CacheDropListener implements Listener, JobLevelListener {

    private final Caches caches;
    private final CacheDrops rules;
    private final DoubleSupplier random;

    CacheDropListener(Caches caches, CacheDrops rules, DoubleSupplier random) {
        this.caches = caches;
        this.rules = rules;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJobAction(JobActionEvent event) {
        if (event.location() == null || random.getAsDouble() >= rules.chance(event.xp())) {
            return;
        }
        event.location()
                .getWorld()
                .dropItemNaturally(event.location(), caches.create(Rarity.COMMON, event.job().id()));
    }

    @Override
    public void onJobLevelChange(UUID uuid, Job job, int levelBefore, int levelAfter) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        for (int level = levelBefore + 1; level <= levelAfter; level++) {
            Rarity rarity = rules.milestone(level).orElse(null);
            if (rarity == null) {
                continue;
            }
            GiveSubcommand.give(player, caches.create(rarity, job.id()));
            player.sendMessage(Component.translatable(
                    "rune.cache.milestone",
                    "Level %s reached: you earn a %s Cache.",
                    Component.text(level),
                    Component.translatable(rarity.translationKey(), rarity.englishName()).color(rarity.color())));
        }
    }
}
