package com.alfgoto.rune.content.quests;

import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.quests.QuestType;
import com.alfgoto.rune.gameplay.quests.QuestTypeRegistry;
import com.alfgoto.rune.gameplay.quests.QuestsModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Puts the Quest Types into the shared registry. For now: one test type (Mining, base size 10). */
@AutoService(RuneModule.class)
public final class CoreQuestsModule implements RuneModule {

    public static final QuestType TEST = new QuestType("test", CoreJobs.MINING, 10);

    @Override
    public void enable(RuneContext ctx) {
        ctx.get(QuestTypeRegistry.class).register(TEST);
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(QuestsModule.class, CoreJobsModule.class);
    }
}
