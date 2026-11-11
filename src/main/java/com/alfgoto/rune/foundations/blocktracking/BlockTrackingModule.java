package com.alfgoto.rune.foundations.blocktracking;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.text.Text;
import com.google.auto.service.AutoService;

/** Wires Block Tracking: the tracker service, its listener and {@code /rune debug blocks}. */
@AutoService(RuneModule.class)
public final class BlockTrackingModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        BlockTracker tracker = ctx.provide(BlockTracker.class, new PdcBlockTracker(Text.NAMESPACE));
        ctx.listen(new BlockTrackingListener(tracker));
        ctx.debug(new BlocksDebug(tracker));
    }
}
