package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** The ore Runes that change blocks: Ore Bloom. (Ore Sense is a visual: see visuals.reveal.OreSenseModule.) */
@AutoService(RuneModule.class)
public final class OreRunesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, ItemsModule.class, BlockTrackingModule.class, CoreRunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new OreBloomListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                ctx.get(BlockTracker.class),
                () -> ThreadLocalRandom.current().nextDouble()));
    }
}
