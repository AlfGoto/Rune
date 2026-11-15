package com.alfgoto.rune.prototype.tree;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** THROWAWAY (issue #138). Adds {@code /rune proto tree ...}. Nothing depends on this module. */
@AutoService(RuneModule.class)
public final class ProtoTreeModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.subcommand(new ProtoCommand(ctx.plugin()));
    }
}
