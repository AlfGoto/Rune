package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.storage.PlayerStorage;
import java.util.UUID;

/**
 * Job XP, Job Levels, Account XP and Account Level of players, stored in {@link PlayerStorage}.
 * Account XP = Job XP gained × the Job's Account Rate.
 */
public final class JobProgress {

    static final String ACCOUNT_XP_KEY = "account/xp";

    private final PlayerStorage storage;
    private final LevelCurve jobCurve;
    private final LevelCurve accountCurve;

    public JobProgress(PlayerStorage storage, LevelCurve jobCurve, LevelCurve accountCurve) {
        this.storage = storage;
        this.jobCurve = jobCurve;
        this.accountCurve = accountCurve;
    }

    public LevelCurve jobCurve() {
        return jobCurve;
    }

    public LevelCurve accountCurve() {
        return accountCurve;
    }

    public double jobXp(UUID player, Job job) {
        return read(player, jobKey(job));
    }

    public int jobLevel(UUID player, Job job) {
        return jobCurve.levelForXp(jobXp(player, job));
    }

    public double accountXp(UUID player) {
        return read(player, ACCOUNT_XP_KEY);
    }

    public int accountLevel(UUID player) {
        return accountCurve.levelForXp(accountXp(player));
    }

    /** Adds Job XP and feeds Account XP with {@code amount × Account Rate}. */
    public void addJobXp(UUID player, Job job, double amount) {
        requireAmount(amount);
        write(player, jobKey(job), jobXp(player, job) + amount);
        write(player, ACCOUNT_XP_KEY, accountXp(player) + amount * job.accountRate());
    }

    /** Sets the Job XP. Account XP is left alone. */
    public void setJobXp(UUID player, Job job, double amount) {
        requireAmount(amount);
        write(player, jobKey(job), amount);
    }

    /** Sets the Job XP to the minimum for {@code level}. Account XP is left alone. */
    public void setJobLevel(UUID player, Job job, int level) {
        write(player, jobKey(job), jobCurve.totalXpForLevel(level));
    }

    private static String jobKey(Job job) {
        return "job/" + job.id() + "/xp";
    }

    private static void requireAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("XP must be a number of 0 or more, got " + amount);
        }
    }

    private double read(UUID player, String key) {
        return storage.get(player, key).map(Double::parseDouble).orElse(0.0);
    }

    private void write(UUID player, String key, double value) {
        storage.set(player, key, Double.toString(value));
    }
}
