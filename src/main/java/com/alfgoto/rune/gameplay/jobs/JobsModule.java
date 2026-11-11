package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.command.LevelSubcommand;
import com.alfgoto.rune.command.XpSubcommand;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.storage.PlayerStorage;
import com.google.auto.service.AutoService;

/** Jobs: Job XP, Job Levels and Account XP, plus /rune xp and /rune level. */
@AutoService(RuneModule.class)
public final class JobsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        RuneConfig config = ctx.config();
        Registry<Job> jobs = CoreJobs.registry();
        JobProgress progress = new JobProgress(
                ctx.get(PlayerStorage.class),
                new LevelCurve(config.levelCurveBase(), config.levelCurveExponent()),
                new LevelCurve(config.accountCurveBase(), config.accountCurveExponent()));
        ctx.provide(Registry.class, jobs);
        ctx.provide(JobProgress.class, progress);
        ctx.subcommand(new XpSubcommand(jobs, progress));
        ctx.subcommand(new LevelSubcommand(jobs, progress));
    }
}
