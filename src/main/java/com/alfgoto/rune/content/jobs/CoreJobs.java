package com.alfgoto.rune.content.jobs;

import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;

/** Job definitions decided in docs/design-decisions.md. Account Rates: Mining 1.0, Woodcutting 0.8. */
public final class CoreJobs {

    public static final Job MINING = Job.builder("mining").accountRate(1.0).build();
    public static final Job WOODCUTTING = Job.builder("woodcutting").accountRate(0.8).build();

    private CoreJobs() {}

    /** Adds every core Job to the shared registry. */
    public static void registerAll(JobRegistry jobs) {
        jobs.register(MINING);
        jobs.register(WOODCUTTING);
    }
}
