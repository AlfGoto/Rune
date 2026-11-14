package com.alfgoto.rune.gameplay.jobs;

/** Hears every Job XP gain. Registered with {@link JobProgress#addListener}. */
@FunctionalInterface
public interface JobXpListener {
    void onJobXpGain(JobXpGain gain);
}
