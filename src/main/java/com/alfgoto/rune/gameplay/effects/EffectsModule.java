package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** Effects framework: an empty EffectRegistry that content modules fill. */
@AutoService(RuneModule.class)
public final class EffectsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.provide(EffectRegistry.class, new EffectRegistry());
    }
}
