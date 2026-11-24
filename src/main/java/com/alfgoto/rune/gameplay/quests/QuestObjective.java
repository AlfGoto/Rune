package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.gameplay.jobs.JobActionEvent;

/** Tells whether one Job Action of the Quest's Job counts as 1 progress for a Quest Type. */
@FunctionalInterface
public interface QuestObjective {

    /** Every Job Action counts. */
    QuestObjective ANY = event -> true;

    boolean counts(JobActionEvent event);
}
