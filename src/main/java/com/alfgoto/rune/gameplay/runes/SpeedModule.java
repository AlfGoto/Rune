package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Mining speed Runes framework: an empty {@link SpeedBonuses} and the listener that applies it. */
@AutoService(RuneModule.class)
public final class SpeedModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        SpeedBonuses bonuses = ctx.provide(SpeedBonuses.class, new SpeedBonuses(ctx.get(ToolLoadout.class)));
        ctx.listen(new SpeedListener(ctx.plugin(), bonuses));
    }
}
