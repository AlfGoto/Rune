package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.foundations.config.RuneConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Rolls a loot table. Pure: it returns the {@link Drop}s and gives nothing.
 *
 * <p>The weight of an entry is {@code base weight × boost ^ (entry Rarity index)}, where the boost is
 * {@code rewards.rarity-boost} for the source Rarity and Common = 0 … Legendary = 4. An entry that is
 * not {@link Reward#available available} weighs 0. The source Rarity also sets how many times the table
 * is rolled ({@code rewards.rolls-by-rarity}).
 */
public final class LootRoller {

    private final RuneConfig config;

    public LootRoller(RuneConfig config) {
        this.config = config;
    }

    /** How many times a source of this context's Rarity rolls the table. */
    public int rolls(RollContext context) {
        return config.rewardsRollsByRarity().get(context.rarity().ordinal());
    }

    /** The weight of {@code entry} in this context. 0 when its reward is not available. */
    public double weight(LootTable.Entry entry, RollContext context) {
        if (!entry.reward().available(context)) {
            return 0;
        }
        double boost = config.rewardsRarityBoost().get(context.rarity().ordinal());
        return entry.baseWeight() * Math.pow(boost, entry.rarity().ordinal());
    }

    /** Rolls the table once per {@link #rolls}. A roll with no available entry gives nothing. */
    public List<Drop> roll(LootTable table, RollContext context, RandomGenerator random) {
        List<Drop> drops = new ArrayList<>();
        for (int i = 0, n = rolls(context); i < n; i++) {
            rollOnce(table, context, random, drops);
        }
        return drops;
    }

    private void rollOnce(LootTable table, RollContext context, RandomGenerator random, List<Drop> into) {
        List<LootTable.Entry> entries = table.entries();
        double[] weights = new double[entries.size()];
        double total = 0;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = weight(entries.get(i), context);
            total += weights[i];
        }
        if (total <= 0) {
            return;
        }
        double point = random.nextDouble() * total;
        int chosen = -1;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] <= 0) {
                continue;
            }
            chosen = i;
            point -= weights[i];
            if (point < 0) {
                break;
            }
        }
        into.add(entries.get(chosen).reward().roll(context, random));
    }
}
