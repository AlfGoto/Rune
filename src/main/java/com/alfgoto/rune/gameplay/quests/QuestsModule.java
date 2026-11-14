package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.storage.PlayerStorage;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;

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
                ThreadLocalRandom.current(),
                QuestsModule::worldDay);
        ctx.provide(QuestTypeRegistry.class, types);
        ctx.provide(QuestBook.class, book);
        ctx.listen(new QuestJoinListener(book));
        ctx.subcommand(new QuestSubcommand(book));
        ctx.debug(new QuestDebug(types.registry(), book));
    }

    /** The world day: the main world's full time divided by the 24000 ticks of a Minecraft day. */
    private static long worldDay() {
        return Bukkit.getWorlds().get(0).getFullTime() / 24000L;
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(JobsModule.class);
    }
}
