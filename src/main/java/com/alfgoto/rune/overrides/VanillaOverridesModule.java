package com.alfgoto.rune.overrides;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** Vanilla Overrides: no enchanting, Locked Gear. */
@AutoService(RuneModule.class)
public final class VanillaOverridesModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new NoEnchantingListener());
        ctx.listen(new LockedGearListener());
    }
}
