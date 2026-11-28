package com.alfgoto.rune.visuals.unlocktree;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.blueprints.Blueprints;
import com.alfgoto.rune.gameplay.blueprints.BlueprintsModule;
import com.alfgoto.rune.gameplay.caches.CacheDrops;
import com.alfgoto.rune.gameplay.caches.CachesModule;
import com.alfgoto.rune.gameplay.crafting.CraftingModule;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The Unlock Tree: Rune's tab in the advancement screen, one line per Job. Visuals only: it reads the Job,
 * recipe, Blueprint and Cache data, and nothing depends on it (ADR 0003). The registries are read one tick
 * after enable, when every content module has filled them (ADR 0004).
 */
@AutoService(RuneModule.class)
public final class UnlockTreeModule implements RuneModule {

    /** Milestone Caches never end: the tree shows them up to this Job Level (config unlock-tree.max-level). */
    static final int DEFAULT_MAX_LEVEL = 50;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                JobsModule.class,
                CraftingModule.class,
                BlueprintsModule.class,
                CachesModule.class,
                ItemsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        JobRegistry jobs = ctx.get(JobRegistry.class);
        RecipeBook recipes = ctx.get(RecipeBook.class);
        Blueprints blueprints = ctx.get(Blueprints.class);
        ItemRegistry items = ctx.get(ItemRegistry.class);
        JobProgress progress = ctx.get(JobProgress.class);
        CacheDrops cacheDrops = new CacheDrops(ctx.config().caches());
        int maxLevel = Math.max(1, ctx.plugin().getConfig().getInt("unlock-tree.max-level", DEFAULT_MAX_LEVEL));

        UnlockTree tree = new UnlockTree(
                new PaperAdvancementBackend(),
                () -> {
                    Map<Job, List<UnlockNode>> lines = new LinkedHashMap<>();
                    for (Job job : jobs.registry().all()) {
                        lines.put(
                                job,
                                UnlockNodes.build(
                                        job,
                                        recipes.all(),
                                        ctx.config().blueprints().milestones(),
                                        blueprints::find,
                                        cacheDrops::milestone,
                                        items::get,
                                        maxLevel));
                    }
                    return lines;
                },
                progress::jobLevel);
        ctx.provide(UnlockTree.class, tree);

        ctx.listen(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                tree.sync(event.getPlayer());
            }
        });
        // Every level change, up or down, from play or from /rune level set and /rune xp set.
        progress.addLevelListener((uuid, job, before, after) -> {
            var player = ctx.plugin().getServer().getPlayer(uuid);
            if (player != null) {
                tree.sync(player);
            }
        });
        // After every module is enabled: build from the registries, then sync players already online (reload).
        ctx.plugin().getServer().getScheduler().runTask(ctx.plugin(), () -> {
            tree.load();
            ctx.plugin().getServer().getOnlinePlayers().forEach(tree::sync);
        });
    }
}
