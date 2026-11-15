package com.alfgoto.rune.gameplay.items;

/**
 * Where a Rune Tool stands in Mastery.
 *
 * @param progress valuable Job Actions counted so far (bone-meal ones count less, so it can have decimals)
 * @param perCapacity actions needed for +1 Capacity
 * @param baseCapacity the tool's Capacity without Mastery
 * @param bonus Capacity earned so far (never above {@code maxBonus})
 * @param maxBonus the most Capacity Mastery can add (a share of the base Capacity, rounded down)
 */
public record MasteryStatus(double progress, int perCapacity, int baseCapacity, int bonus, int maxBonus) {

    /** Mastery is finished: no more Capacity to earn. */
    public boolean maxed() {
        return bonus >= maxBonus;
    }

    /** Capacity with Mastery: base + bonus. */
    public int capacity() {
        return baseCapacity + bonus;
    }

    /** Actions counted towards the next +1 Capacity (0 when maxed). */
    public int towardsNext() {
        return maxed() ? 0 : (int) Math.floor(progress - (double) bonus * perCapacity + 1e-9);
    }
}
