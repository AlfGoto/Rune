package com.alfgoto.rune.gameplay.jobs;

import java.util.UUID;

/** One Job XP gain, with the Job Level and Account Level before and after it. */
public record JobXpGain(
        UUID player,
        Job job,
        double amount,
        int jobLevelBefore,
        int jobLevelAfter,
        int accountLevelBefore,
        int accountLevelAfter) {

    public boolean jobLeveledUp() {
        return jobLevelAfter > jobLevelBefore;
    }

    public boolean accountLeveledUp() {
        return accountLevelAfter > accountLevelBefore;
    }
}
