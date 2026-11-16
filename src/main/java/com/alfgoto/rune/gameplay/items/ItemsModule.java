package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;

/** Items framework: an empty ItemRegistry and /rune give. Content modules fill the registry. */
@AutoService(RuneModule.class)
public final class ItemsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry registry =
                ctx.provide(ItemRegistry.class, new ItemRegistry(ctx.plugin(), ctx.config().tools(), ctx.config().armor()));
        ctx.provide(ToolMastery.class, new ToolMastery(ctx.plugin(), registry, ctx.config().tools()));
        GiveSubcommand give = ctx.provide(GiveSubcommand.class, new GiveSubcommand(registry));
        ctx.subcommand(give);
        Codex codex = ctx.provide(Codex.class, new Codex(registry));
        ctx.listen(new CodexListener(codex));
        ctx.listen(ctx.provide(BrokenToolListener.class, new BrokenToolListener(registry)));
    }
}
