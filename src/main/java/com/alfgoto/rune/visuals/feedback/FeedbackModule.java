package com.alfgoto.rune.visuals.feedback;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.gameplay.quests.QuestsModule;
import com.alfgoto.rune.gameplay.runes.ActiveRunes;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Wires Feedback: listens to Job XP gains and Active Rune use, and shows them to the player. */
@AutoService(RuneModule.class)
public final class FeedbackModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.get(JobProgress.class).addListener(new XpFeedback());
        ctx.get(ActiveRunes.class).addListener(new ActiveRuneFeedback());
        ctx.listen(new QuestFeedback());
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(JobsModule.class, RunesModule.class, QuestsModule.class);
    }
}
