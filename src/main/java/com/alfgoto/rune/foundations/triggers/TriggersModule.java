package com.alfgoto.rune.foundations.triggers;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** Wires Triggers: Paper events become Rune events. Other modules read {@link Triggers} from the context. */
@AutoService(RuneModule.class)
public final class TriggersModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        Triggers triggers = ctx.provide(Triggers.class, new Triggers(ctx.plugin().getServer()));
        EventDebug eventDebug = new EventDebug(ctx.logger());
        ctx.listen(triggers);
        ctx.listen(eventDebug);
        ctx.debug(new EventsDebug(eventDebug));
    }
}
