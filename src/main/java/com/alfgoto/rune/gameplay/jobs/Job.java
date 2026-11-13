package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.text.Text;
import java.util.Objects;

/**
 * A Job definition. Built with {@link #builder(String)} and registered by id.
 *
 * <p>The Account Rate is how fast this Job feeds Account XP (Mining = 1.0, the reference).
 */
public final class Job implements Identified {

    private final String id;
    private final double accountRate;

    private final int questMinLevel;

    private Job(String id, double accountRate, int questMinLevel) {
        this.id = id;
        this.accountRate = accountRate;
        this.questMinLevel = questMinLevel;
    }

    /** Quest Eligibility: the Job Level needed before this Job supplies Quests (1 gathering, 2 crafting-type). */
    public int questMinLevel() {
        return questMinLevel;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    @Override
    public String id() {
        return id;
    }

    /** Account XP gained per Job XP gained. */
    public double accountRate() {
        return accountRate;
    }

    /** Translation key of the Job name, e.g. {@code rune.job.mining}. */
    public String translationKey() {
        return Text.key("job", id);
    }

    /** English name when no language file has the key: the id with a capital, e.g. {@code Mining}. */
    public String fallbackName() {
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    public static final class Builder {
        private final String id;
        private double accountRate = 1.0;
        private int questMinLevel = 1;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder questMinLevel(int questMinLevel) {
            this.questMinLevel = questMinLevel;
            return this;
        }

        public Builder accountRate(double accountRate) {
            this.accountRate = accountRate;
            return this;
        }

        public Job build() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("Job id must not be blank");
            }
            if (!Double.isFinite(accountRate) || accountRate <= 0) {
                throw new IllegalArgumentException("Account Rate must be greater than 0, got " + accountRate);
            }
            if (questMinLevel < 1) {
                throw new IllegalArgumentException("Quest min level must be 1 or more, got " + questMinLevel);
            }
            return new Job(id, accountRate, questMinLevel);
        }
    }
}
