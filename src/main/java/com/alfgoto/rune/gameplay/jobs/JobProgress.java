package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.storage.PlayerStorage;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job XP, Job Levels, Account XP and Account Level of players, stored in {@link PlayerStorage}.
 * Account XP = Job XP gained × the Job's Account Rate.
 */
public final class JobProgress {

    private static final Logger LOGGER = Logger.getLogger(JobProgress.class.getName());

    static final String ACCOUNT_XP_KEY = "account/xp";

    private final PlayerStorage storage;
    private final LevelCurve jobCurve;
    private final LevelCurve accountCurve;
    private final List<JobXpListener> listeners = new CopyOnWriteArrayList<>();
    private final List<JobLevelListener> levelListeners = new CopyOnWriteArrayList<>();

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

    /** Called after every {@link #addJobXp}. Lets Visuals react without Gameplay depending on them. */
    public void addListener(JobXpListener listener) {
        listeners.add(listener);
    }

    /** Called whenever a Job Level changes, whatever the cause. Lets Crafting react to levels. */
    public void addLevelListener(JobLevelListener listener) {
        levelListeners.add(listener);
    }

    /**
     * Adds Job XP and feeds Account XP with {@code amount × Account Rate}.
     * Run on the main thread: listeners call Bukkit player methods.
     */
    public void addJobXp(UUID player, Job job, double amount) {
        requireAmount(amount);
        int jobLevelBefore = jobLevel(player, job);
        int accountLevelBefore = accountLevel(player);
        write(player, jobKey(job), jobXp(player, job) + amount);
        write(player, ACCOUNT_XP_KEY, accountXp(player) + amount * job.accountRate());
        JobXpGain gain = new JobXpGain(
                player,
                job,
                amount,
                jobLevelBefore,
                jobLevel(player, job),
                accountLevelBefore,
                accountLevel(player));
        for (JobXpListener listener : listeners) {
            try {
                listener.onJobXpGain(gain);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "A Job XP listener failed", e);
            }
        }
        notifyLevel(player, job, gain.jobLevelBefore(), gain.jobLevelAfter());
    }

    /**
     * Adds Account XP that no Job XP produced (a reward). Job XP is left alone. No Job XP listener
     * runs: there is no Job gain to show.
     */
    public void addAccountXpBonus(UUID player, double amount) {
        requireAmount(amount);
        write(player, ACCOUNT_XP_KEY, accountXp(player) + amount);
    }

    /** Sets the Job XP. Account XP is left alone. */
    public void setJobXp(UUID player, Job job, double amount) {
        requireAmount(amount);
        int before = jobLevel(player, job);
        write(player, jobKey(job), amount);
        notifyLevel(player, job, before, jobLevel(player, job));
    }

    /** Sets the Job XP to the minimum for {@code level}. Account XP is left alone. */
    public void setJobLevel(UUID player, Job job, int level) {
        int before = jobLevel(player, job);
        write(player, jobKey(job), jobCurve.totalXpForLevel(level));
        notifyLevel(player, job, before, jobLevel(player, job));
    }

    private void notifyLevel(UUID player, Job job, int before, int after) {
        if (before == after) {
            return;
        }
        for (JobLevelListener listener : levelListeners) {
            try {
                listener.onJobLevelChange(player, job, before, after);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "A Job Level listener failed", e);
            }
        }
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
