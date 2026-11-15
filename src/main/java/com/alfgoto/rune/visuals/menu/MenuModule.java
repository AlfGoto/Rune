package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** The Interface module: click protection for every {@link Menu}, and {@code /rune debug menu}. */
@AutoService(RuneModule.class)
public final class MenuModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new MenuListener());
        ctx.debug(new MenuDebug());
    }
}
