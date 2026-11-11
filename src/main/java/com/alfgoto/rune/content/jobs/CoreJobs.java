package com.alfgoto.rune.content.jobs;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.jobs.Job;

/** Job definitions decided in docs/design-decisions.md. Account Rates: Mining 1.0, Woodcutting 0.8. */
public final class CoreJobs {

    public static final Job MINING = Job.builder("mining").accountRate(1.0).build();
    public static final Job WOODCUTTING = Job.builder("woodcutting").accountRate(0.8).build();

    private CoreJobs() {}

    /** A new registry holding every Job. */
    public static Registry<Job> registry() {
        Registry<Job> jobs = new Registry<>("jobs");
        jobs.register(MINING);
        jobs.register(WOODCUTTING);
        return jobs;
    }
}
