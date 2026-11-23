package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.Reward;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * One Rune of a given Rarity (Rank 1).
 *
 * <p>With a Job in the context, it picks that Job's Runes with the chance {@code rewards.job-bias},
 * otherwise general Runes. Without a Job it only picks general Runes. If the chosen pool has no Rune of
 * that Rarity it uses the other pool. If both are empty it steps down one Rarity and tries again.
 * If it finds nothing at all, it is not available.
 */
public final class RuneReward implements Reward {

    private final RuneRegistry runes;
    private final RuneConfig config;
    private final Rarity rarity;

    public RuneReward(RuneRegistry runes, RuneConfig config, Rarity rarity) {
        this.runes = runes;
        this.config = config;
        this.rarity = rarity;
    }

    @Override
    public boolean available(RollContext context) {
        for (Rarity r = rarity; r != null; r = lower(r)) {
            if (!pool(r, context.jobId()).isEmpty() || !pool(r, null).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Drop roll(RollContext context, RandomGenerator random) {
        boolean wantJob = context.jobId() != null && random.nextDouble() < config.rewardsJobBias();
        for (Rarity r = rarity; r != null; r = lower(r)) {
            List<Rune> job = pool(r, context.jobId());
            List<Rune> general = pool(r, null);
            List<Rune> chosen = wantJob ? job : general;
            if (chosen.isEmpty()) {
                chosen = wantJob ? general : job;
            }
            if (!chosen.isEmpty()) {
                return new RuneDrop(runes, chosen.get(random.nextInt(chosen.size())));
            }
        }
        throw new IllegalStateException("No Rune of Rarity " + rarity + " or lower. available() was false.");
    }

    /** Runes of this Rarity that belong to {@code jobId}, or the general Runes when it is null. */
    private List<Rune> pool(Rarity r, String jobId) {
        return runes.all().stream()
                .filter(rune -> rune.rarity() == r)
                .filter(rune -> jobId == null
                        ? rune.job().isEmpty()
                        : rune.job().filter(job -> job.id().equals(jobId)).isPresent())
                .toList();
    }

    private static Rarity lower(Rarity r) {
        return r.ordinal() == 0 ? null : Rarity.values()[r.ordinal() - 1];
    }

    private record RuneDrop(RuneRegistry runes, Rune rune) implements Drop {

        @Override
        public Component describe() {
            return Component.translatable(rune.item().translationKey(), rune.item().fallbackName())
                    .color(rune.rarity().color());
        }

        @Override
        public void give(Player player) {
            GiveSubcommand.give(player, runes.create(rune, 1));
        }

        @Override
        public org.bukkit.inventory.ItemStack icon() {
            return runes.create(rune, 1);
        }
    }
}
