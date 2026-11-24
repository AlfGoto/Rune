package com.alfgoto.rune.overrides;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.gameplay.caches.CachesModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Vanilla Overrides: no enchanting, Locked Gear, enchanted books in loot replaced by Rune Caches. */
@AutoService(RuneModule.class)
public final class VanillaOverridesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(CachesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new NoEnchantingListener());
        ctx.listen(new LockedGearListener());
        // One tick later, so every recipe exists. The listener's craft checks stay as a safety net.
        ctx.plugin().getServer().getScheduler().runTask(
                ctx.plugin(), () -> LockedGearRecipes.removeAll(ctx.plugin().getServer()));
        ctx.listen(new BookLootListener(ctx.get(Caches.class), ctx.config().bookCacheRarity()));
    }
}
