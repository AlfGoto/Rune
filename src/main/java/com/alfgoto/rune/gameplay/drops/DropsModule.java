package com.alfgoto.rune.gameplay.drops;

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

/** Drop-changing Runes (Fortune, Smelt, Silk Touch): changes the drops of blocks broken with a Rune Tool. */
@AutoService(RuneModule.class)
public final class DropsModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class, BlockTrackingModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new DropEffectsListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                new Smelting(),
                () -> ThreadLocalRandom.current().nextDouble(),
                ctx.get(BlockTracker.class)));
        ctx.listen(new BonusGemsListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                ctx.get(BlockTracker.class),
                () -> ThreadLocalRandom.current().nextDouble()));
        ctx.listen(new MagnetListener(ctx.get(ItemRegistry.class), ctx.get(ToolLoadout.class)));
    }
}
