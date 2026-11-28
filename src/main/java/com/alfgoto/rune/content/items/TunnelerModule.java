package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Starts the Tunneler Pickaxe's 3x3 mining. The tool itself is registered by {@link CoreItemsModule}. */
@AutoService(RuneModule.class)
public final class TunnelerModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class, TriggersModule.class, CoreItemsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new TunnelerListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                new MultiBreak(ctx.get(Triggers.class)),
                ctx.get(BrokenToolListener.class)::isBroken));
    }
}
