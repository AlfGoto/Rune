package com.alfgoto.rune.gameplay.durability;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Durability Runes (Sturdy): less durability use on Rune Tools. */
@AutoService(RuneModule.class)
public final class DurabilityModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new SturdyListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                () -> ThreadLocalRandom.current().nextDouble()));
    }
}
