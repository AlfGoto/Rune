package com.alfgoto.rune.visuals.feedback;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Wires Feedback: listens to Job XP gains and shows them to the player. */
@AutoService(RuneModule.class)
public final class FeedbackModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.get(JobProgress.class).addListener(new XpFeedback());
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(JobsModule.class);
    }
}
