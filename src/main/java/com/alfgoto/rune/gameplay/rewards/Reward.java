package com.alfgoto.rune.gameplay.rewards;

import java.util.random.RandomGenerator;

/**
 * One kind of reward a loot table entry can give. Each kind lives in the module that owns the thing
 * (Runes, items, Jobs, Quests), so Rewards knows no reward kind and imports none of them.
 */
public interface Reward {

    /** False when this reward cannot be given in this context (e.g. Job XP with no Job). Its weight is then 0. */
    boolean available(RollContext context);

    /** Rolls the result. Only called when {@link #available} is true. All randomness comes from {@code random}. */
    Drop roll(RollContext context, RandomGenerator random);
}
