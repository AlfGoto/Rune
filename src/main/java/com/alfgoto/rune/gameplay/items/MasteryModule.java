package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.BlockXpRules;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/**
 * Mastery: Rune Tools earn Capacity from valuable Job Actions, plus {@code /rune debug mastery}.
 * Block Tracking clears a block's tags when it breaks, so this depends on it (and so on Triggers)
 * to read the tags first.
 */
@AutoService(RuneModule.class)
public final class MasteryModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ToolMastery mastery = ctx.get(ToolMastery.class);
        ctx.listen(new MasteryListener(
                ctx.get(ItemRegistry.class), mastery, ctx.get(BlockXpRules.class), ctx.get(BlockTracker.class)));
        ctx.debug(new MasteryDebug(mastery));
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, JobsModule.class, BlockTrackingModule.class);
    }
}
