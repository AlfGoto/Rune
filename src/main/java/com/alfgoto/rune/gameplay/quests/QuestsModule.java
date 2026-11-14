package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.storage.PlayerStorage;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Quests framework: Quest Types × Quest Tiers, 3 active per player, /rune quest. Content adds the Quest Types. */
@AutoService(RuneModule.class)
public final class QuestsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        QuestTypeRegistry types = new QuestTypeRegistry();
        QuestBook book = new QuestBook(
                ctx.get(PlayerStorage.class),
                ctx.get(JobProgress.class),
                types.registry(),
                ctx.config(),
                ThreadLocalRandom.current());
        ctx.provide(QuestTypeRegistry.class, types);
        ctx.provide(QuestBook.class, book);
        ctx.listen(new QuestJoinListener(book));
        ctx.subcommand(new QuestSubcommand(book));
        ctx.debug(new QuestDebug(types.registry(), book));
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(JobsModule.class);
    }
}
