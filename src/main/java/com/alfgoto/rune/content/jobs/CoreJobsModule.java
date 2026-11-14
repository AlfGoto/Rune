package com.alfgoto.rune.content.jobs;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.BlockActions;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Puts Mining and Woodcutting into the shared Job registry. */
@AutoService(RuneModule.class)
public final class CoreJobsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        JobRegistry jobs = ctx.get(JobRegistry.class);
        CoreJobs.registerAll(jobs);
        CoreBlockActions.registerAll(ctx.get(BlockActions.class), jobs);
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(JobsModule.class);
    }
}
