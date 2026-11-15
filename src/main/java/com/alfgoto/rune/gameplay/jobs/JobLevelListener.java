package com.alfgoto.rune.gameplay.jobs;

import java.util.UUID;

/**
 * Hears every change of a Job Level: XP gains that cross a level, {@code /rune xp set} and
 * {@code /rune level set}, up or down. Registered with {@link JobProgress#addLevelListener}.
 */
@FunctionalInterface
public interface JobLevelListener {
    void onJobLevelChange(UUID player, Job job, int levelBefore, int levelAfter);
}
