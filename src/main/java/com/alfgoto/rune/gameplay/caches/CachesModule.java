package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.gameplay.rewards.LootRoller;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.gameplay.rewards.RewardsModule;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Rune Caches: registers one Cache item per Rarity, opens them on right-click and adds {@code /rune give cache}. */
@AutoService(RuneModule.class)
public final class CachesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RewardsModule.class, JobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        Caches.itemsToRegister().forEach(items::register);
        Caches caches = ctx.provide(
                Caches.class,
                new Caches(
                        ctx.plugin(),
                        items,
                        ctx.get(LootTableRegistry.class),
                        ctx.get(LootRoller.class),
                        ThreadLocalRandom.current()));
        ctx.get(GiveSubcommand.class).addVariant(new GiveCacheVariant(caches, ctx.get(JobRegistry.class)));
        ctx.listen(new CacheListener(caches));
    }
}
