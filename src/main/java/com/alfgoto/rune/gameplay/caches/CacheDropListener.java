package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.jobs.JobLevelListener;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Gives Caches for Jobs: a chance of a Cache at the block on every Job Action (Rarity from config, none in Creative),
 * and the Milestone Cache when a Job Level goes up. Both Caches are for the Job that earned them.
 */
final class CacheDropListener implements Listener, JobLevelListener {

    /** Effect id of Cache Hunter: more Cache chance, in percent. */
    static final String CACHE_HUNTER = "cache_hunter";

    private final Caches caches;
    private final CacheDrops rules;
    private final DoubleSupplier random;
    private final ItemRegistry items;
    private final ToolLoadout loadout;

    CacheDropListener(
            Caches caches, CacheDrops rules, DoubleSupplier random, ItemRegistry items, ToolLoadout loadout) {
        this.caches = caches;
        this.rules = rules;
        this.random = random;
        this.items = items;
        this.loadout = loadout;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJobAction(JobActionEvent event) {
        if (event.location() == null || event.player().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (random.getAsDouble() >= rules.chance(event.xp(), cacheHunter(event))) {
            return;
        }
        event.location()
                .getWorld()
                .dropItemNaturally(event.location(), caches.create(rules.dropRarity(), event.job().id()));
    }

    /** The stacked Cache Hunter value (Trait and Runes) of the tool of the action, 0 without a Rune Tool. */
    private double cacheHunter(JobActionEvent event) {
        ItemStack tool = event.tool();
        if (tool == null || items.of(tool).isEmpty()) {
            return 0;
        }
        return Stacking.total(CACHE_HUNTER, loadout.sources(tool, event.player()));
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
                    "Level %s reached: %s Cache earned.",
                    Component.text(level),
                    Text.rarityName(rarity)));
        }
    }
}
