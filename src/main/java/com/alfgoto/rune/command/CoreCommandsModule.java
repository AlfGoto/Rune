package com.alfgoto.rune.command;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** Subcommands that belong to no other module: {@code /rune version}. */
@AutoService(RuneModule.class)
public final class CoreCommandsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.subcommand(new VersionSubcommand(ctx.plugin().getPluginMeta().getVersion()));
    }
}
