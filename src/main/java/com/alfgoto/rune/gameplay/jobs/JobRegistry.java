package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.foundations.registry.Registry;

/**
 * The shared Job registry. Its own type, so it can be a service next to the Rune and Quest registries.
 * Starts empty: content modules fill it.
 */
public final class JobRegistry {

    private final Registry<Job> jobs = new Registry<>("jobs");

    /** Adds a Job. Throws if its id is already used. */
    public Job register(Job job) {
        return jobs.register(job);
    }

    /** The underlying registry. Read it when you need it: content modules add Jobs after enable. */
    public Registry<Job> registry() {
        return jobs;
    }
}
