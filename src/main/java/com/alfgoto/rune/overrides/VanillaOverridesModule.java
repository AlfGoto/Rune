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
        ctx.listen(new BookLootListener(ctx.get(Caches.class), ctx.config().bookCacheRarity()));
    }
}
