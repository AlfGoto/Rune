package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.command.LevelSubcommand;
import com.alfgoto.rune.command.XpSubcommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.storage.PlayerStorage;
import com.google.auto.service.AutoService;

/** Jobs framework: Job XP, Job Levels and Account XP, plus /rune xp and /rune level. Content adds the Jobs. */
@AutoService(RuneModule.class)
public final class JobsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        RuneConfig config = ctx.config();
        JobRegistry jobs = new JobRegistry();
        JobProgress progress = new JobProgress(
                ctx.get(PlayerStorage.class),
                new LevelCurve(config.levelCurveBase(), config.levelCurveExponent()),
                new LevelCurve(config.accountCurveBase(), config.accountCurveExponent()));
        ctx.provide(JobRegistry.class, jobs);
        ctx.provide(JobProgress.class, progress);
        // The commands read the registry when they run, so Jobs added later by content show up.
        ctx.subcommand(new XpSubcommand(jobs.registry(), progress));
        ctx.subcommand(new LevelSubcommand(jobs.registry(), progress));
    }
}
