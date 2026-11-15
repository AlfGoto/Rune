package com.alfgoto.rune.foundations.trees;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Wires Felled Tree detection: the shared {@link TreeScan} service and the {@code TreeFelled} Rune event. */
@AutoService(RuneModule.class)
public final class TreesModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        TreeScan scan = ctx.provide(TreeScan.class, new TreeScan(ctx.get(BlockTracker.class)));
        ctx.listen(new TreeFelledListener(ctx.plugin().getServer(), new FellingSessions(scan, System::currentTimeMillis)));
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(TriggersModule.class, BlockTrackingModule.class);
    }
}
