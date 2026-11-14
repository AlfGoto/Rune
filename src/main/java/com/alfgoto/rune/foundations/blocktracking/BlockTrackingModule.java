package com.alfgoto.rune.foundations.blocktracking;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.text.Text;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Wires Block Tracking: the tracker service, its listener and {@code /rune debug blocks}. */
@AutoService(RuneModule.class)
public final class BlockTrackingModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        BlockTracker tracker = ctx.provide(BlockTracker.class, new PdcBlockTracker(Text.NAMESPACE));
        ctx.listen(new BlockTrackingListener(tracker));
        ctx.debug(new BlocksDebug(tracker));
    }

    /**
     * Triggers must be enabled first: its break listener and ours both run at MONITOR, and Job XP reads a block's
     * tags from the Rune break event before this module clears them.
     */
    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(TriggersModule.class);
    }
}
