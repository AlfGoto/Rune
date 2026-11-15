package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.Reward;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Text;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/** Job XP in the Job of the roll. Needs a Job in the context, so it is not available without one. */
public final class JobXpReward implements Reward {

    private final JobRegistry jobs;
    private final JobProgress progress;
    private final double amount;

    public JobXpReward(JobRegistry jobs, JobProgress progress, double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Job XP must be greater than 0, got " + amount);
        }
        this.jobs = jobs;
        this.progress = progress;
        this.amount = amount;
    }

    @Override
    public boolean available(RollContext context) {
        return context.job().flatMap(id -> jobs.registry().find(id)).isPresent();
    }

    @Override
    public Drop roll(RollContext context, RandomGenerator random) {
        Job job = jobs.registry().get(context.jobId());
        return new Drop() {
            @Override
            public Component describe() {
                return Text.translatable(
                        "rune.reward.job_xp",
                        "+%s %s XP",
                        Component.text(Numbers.format(amount)),
                        Component.translatable(job.translationKey(), job.fallbackName()));
            }

            @Override
            public void give(Player player) {
                progress.addJobXp(player.getUniqueId(), job, amount);
            }
        };
    }
}
