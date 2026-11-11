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

    private Job(String id, double accountRate) {
        this.id = id;
        this.accountRate = accountRate;
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

    public static final class Builder {
        private final String id;
        private double accountRate = 1.0;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
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
            return new Job(id, accountRate);
        }
    }
}
