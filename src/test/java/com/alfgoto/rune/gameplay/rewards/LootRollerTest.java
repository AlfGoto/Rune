package com.alfgoto.rune.gameplay.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class LootRollerTest {

    private static final UUID PLAYER = UUID.randomUUID();

    /** What the stub reward gives: the name of the entry it came from. */
    private record StubDrop(String name) implements Drop {
        @Override
        public Component describe() {
            return Component.text(name);
        }

        @Override
        public void give(Player player) {}
    }

    private record Stub(String name, boolean available) implements Reward {
        @Override
        public boolean available(RollContext context) {
            return available;
        }

        @Override
        public Drop roll(RollContext context, RandomGenerator random) {
            return new StubDrop(name);
        }
    }

    private final LootRoller roller = new LootRoller(RuneConfig.defaults());

    /** The five Rune entries of the cache table: base weights 1000 / 400 / 150 / 40 / 5. */
    private static LootTable runeTable() {
        double[] weights = {1000, 400, 150, 40, 5};
        LootTable.Builder builder = LootTable.builder("runes");
        for (Rarity rarity : Rarity.values()) {
            builder.add(new Stub(rarity.id(), true), weights[rarity.ordinal()], rarity);
        }
        return builder.build();
    }

    /** Rune Rarity odds (%) per source Rarity, from docs/rewards.md. */
    private static final double[][] ODDS = {
        {63, 25, 9, 3, 0.3}, // Common
        {48, 29, 16, 6, 1}, // Uncommon
        {36, 29, 21, 11, 3}, // Rare
        {20, 24, 27, 21, 8}, // Epic
        {11, 18, 27, 29, 14}, // Legendary
    };

    @Test
    void oddsShiftByRarity() {
        LootTable table = runeTable();
        RandomGenerator random = new java.util.Random(42);
        for (Rarity source : Rarity.values()) {
            RollContext context = RollContext.of(PLAYER, source);
            Map<String, Integer> counts = new HashMap<>();
            int total = 0;
            while (total < 200_000) {
                for (Drop drop : roller.roll(table, context, random)) {
                    counts.merge(((StubDrop) drop).name(), 1, Integer::sum);
                    total++;
                }
            }
            for (Rarity entry : Rarity.values()) {
                double percent = 100.0 * counts.getOrDefault(entry.id(), 0) / total;
                assertEquals(
                        ODDS[source.ordinal()][entry.ordinal()],
                        percent,
                        1.0,
                        entry + " odds from a " + source + " source");
            }
        }
    }

    @Test
    void weightIsBaseTimesBoostToTheEntryRarityIndex() {
        LootTable.Entry epic = new LootTable.Entry(new Stub("e", true), 40, Rarity.EPIC);
        assertEquals(40, roller.weight(epic, RollContext.of(PLAYER, Rarity.COMMON)), 1e-9);
        assertEquals(40 * 27, roller.weight(epic, RollContext.of(PLAYER, Rarity.EPIC)), 1e-9); // 3^3
        assertEquals(40 * 64, roller.weight(epic, RollContext.of(PLAYER, Rarity.LEGENDARY)), 1e-9); // 4^3
    }

    @Test
    void unavailableEntriesAreNeverPicked() {
        LootTable table = LootTable.builder("t")
                .add(new Stub("gone", false), 1_000_000, Rarity.COMMON)
                .add(new Stub("here", true), 1, Rarity.COMMON)
                .build();
        RandomGenerator random = new java.util.Random(1);
        RollContext context = RollContext.of(PLAYER, Rarity.LEGENDARY);
        assertEquals(0, roller.weight(table.entries().get(0), context));
        for (int i = 0; i < 2000; i++) {
            for (Drop drop : roller.roll(table, context, random)) {
                assertEquals("here", ((StubDrop) drop).name());
            }
        }
    }

    @Test
    void aTableWithNothingAvailableGivesNothing() {
        LootTable table = LootTable.builder("t").add(new Stub("gone", false), 5, Rarity.COMMON).build();
        assertTrue(roller.roll(table, RollContext.of(PLAYER, Rarity.EPIC), new java.util.Random(1)).isEmpty());
    }

    @Test
    void rollCountFollowsTheRarity() {
        LootTable table = LootTable.builder("t").add(new Stub("x", true), 1, Rarity.COMMON).build();
        RandomGenerator random = new java.util.Random(1);
        List<Integer> expected = List.of(1, 1, 2, 2, 3);
        for (Rarity rarity : Rarity.values()) {
            RollContext context = RollContext.of(PLAYER, rarity);
            assertEquals(expected.get(rarity.ordinal()), roller.rolls(context));
            assertEquals(expected.get(rarity.ordinal()), roller.roll(table, context, random).size());
        }
    }

    @Test
    void rollingIsDeterministicForASeed() {
        LootTable table = runeTable();
        RollContext context = RollContext.of(PLAYER, Rarity.LEGENDARY);
        assertEquals(
                roller.roll(table, context, new java.util.Random(9)),
                roller.roll(table, context, new java.util.Random(9)));
    }
}
