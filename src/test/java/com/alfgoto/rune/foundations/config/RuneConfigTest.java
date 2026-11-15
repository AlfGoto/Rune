package com.alfgoto.rune.foundations.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class RuneConfigTest {

    private final List<String> errors = new ArrayList<>();
    private final Logger logger = Logger.getAnonymousLogger();

    RuneConfigTest() {
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord r) {
                if (r.getLevel() == Level.SEVERE) {
                    errors.add(r.getMessage());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    private RuneConfig load(String yaml) throws Exception {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(yaml);
        return RuneConfig.load(cfg, logger);
    }

    @Test
    void validConfigIsReadWithoutErrors() throws Exception {
        RuneConfig c = load("""
                jobs:
                  level-curve:
                    base: 40
                    exponent: 1.5
                  bonemeal-multiplier: 0.5
                  block-xp:
                    STONE: 2
                account:
                  level-curve:
                    base: 100
                    exponent: 1.1
                capacity:
                  rune-cost:
                    base: [1, 2, 3, 5, 8]
                    per-rank: 1
                quests:
                  tier-multipliers: [1, 2, 3, 4, 6]
                  tier-min-levels: [1, 2, 3, 4, 5]
                  tier-weights:
                    base: [1, 2, 3, 4, 5]
                    per-level: [-1, 0, 1, 2, 3]
                  max-reroll-tokens: 5
                tools:
                  speed-by-tier: [1, 2, 3.5]
                  epic-speed: 9
                  durability-by-tier: [10, 20, 30]
                  epic-durability: 40
                  slots-by-rarity: [2, 3, 4, 5, 6]
                  capacity-by-rarity: [1, 2, 3, 4, 5]
                  mastery:
                    pickaxe-actions: 100
                    axe-actions: 200
                    max-bonus: 0.5
                """);
        assertEquals(100, c.tools().pickaxeMasteryActions());
        assertEquals(200, c.tools().axeMasteryActions());
        assertEquals(0.5, c.tools().masteryMaxBonus());
        assertEquals(List.of(1.0, 2.0, 3.5), c.tools().speedByTier());
        assertEquals(9, c.tools().epicSpeed());
        assertEquals(List.of(10, 20, 30), c.tools().durabilityByTier());
        assertEquals(40, c.tools().epicDurability());
        assertEquals(List.of(2, 3, 4, 5, 6), c.tools().slotsByRarity());
        assertEquals(List.of(1, 2, 3, 4, 5), c.tools().capacityByRarity());
        assertEquals(5, c.questMaxRerollTokens());
        assertEquals(List.of(1.0, 2.0, 3.0, 5.0, 8.0), c.runeCostBase());
        assertEquals(1, c.runeCostPerRank());
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0), c.questTierMinLevels());
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0), c.questTierWeightBase());
        assertEquals(List.of(-1.0, 0.0, 1.0, 2.0, 3.0), c.questTierWeightPerLevel());
        assertEquals(40, c.levelCurveBase());
        assertEquals(1.5, c.levelCurveExponent());
        assertEquals(0.5, c.bonemealXpMultiplier());
        assertEquals(java.util.Map.of(org.bukkit.Material.STONE, 2.0), c.blockXp());
        assertEquals(100, c.accountCurveBase());
        assertEquals(1.1, c.accountCurveExponent());
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 6.0), c.questTierMultipliers());
        assertTrue(errors.isEmpty());
    }

    @Test
    void shippedDefaultConfigIsValid() throws Exception {
        try (var in = getClass().getResourceAsStream("/config.yml")) {
            assertNotNull(in);
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            RuneConfig c = RuneConfig.load(cfg, logger);
            assertTrue(errors.isEmpty(), errors.toString());
            assertEquals(RuneConfig.defaults().questTierMultipliers(), c.questTierMultipliers());
            assertEquals(50 * Math.pow(10, 1.3), c.xpToNextLevel(10), 1e-9);
            assertEquals(0.25, c.bonemealXpMultiplier());
        }
    }

    @Test
    void badValuesLogAndFallBack() throws Exception {
        RuneConfig c = load("""
                jobs:
                  level-curve:
                    base: -5
                    exponent: banana
                  bonemeal-multiplier: 0.25
                  block-xp:
                    STONE: 1
                account:
                  level-curve:
                    base: 200
                    exponent: 1.3
                capacity:
                  rune-cost:
                    base: [1, 2, 3, 5, 8]
                    per-rank: 1
                quests:
                  tier-multipliers: [1, 2]
                  tier-min-levels: [1, 1, 1, 15, 25]
                  tier-weights:
                    base: [100, 60, 30, 10, 5]
                    per-level: [-2, 0, 1, 1.5, 1.5]
                  max-reroll-tokens: 3
                tools:
                  speed-by-tier: [4, 6, 8]
                  epic-speed: 12
                  durability-by-tier: [131, 250, 1561]
                  epic-durability: 2031
                  slots-by-rarity: [1, 2, 3, 4, 5]
                  capacity-by-rarity: [3, 6, 10, 14, 18]
                  mastery:
                    pickaxe-actions: 500
                    axe-actions: 1000
                    max-bonus: 0.75
                """);
        assertEquals(50, c.levelCurveBase());
        assertEquals(1.3, c.levelCurveExponent());
        assertEquals(RuneConfig.defaults().questTierMultipliers(), c.questTierMultipliers());
        assertEquals(3, errors.size());
        assertTrue(errors.get(0).contains("jobs.level-curve.base"));
        assertTrue(errors.get(1).contains("jobs.level-curve.exponent"));
        assertTrue(errors.get(2).contains("quests.tier-multipliers"));
    }

    @Test
    void missingValuesLogAndFallBack() throws Exception {
        RuneConfig c = load("");
        assertEquals(22, errors.size());
        assertEquals(RuneConfig.defaults().runeCostBase(), c.runeCostBase());
        assertEquals(1, c.runeCostPerRank());
        assertTrue(c.blockXp().isEmpty());
        assertEquals(0.25, c.bonemealXpMultiplier());
        assertEquals(3, c.questMaxRerollTokens());
        assertEquals(50, c.levelCurveBase());
        assertEquals(200, c.accountCurveBase());
        assertEquals(1.3, c.accountCurveExponent());
    }

    @Test
    void badBlockXpEntriesAreSkippedAndLogged() throws Exception {
        RuneConfig c = load("""
                jobs:
                  block-xp:
                    STONE: 1
                    NOT_A_BLOCK: 3
                    DIRT: -1
                    SAND: banana
                """);
        assertEquals(java.util.Map.of(org.bukkit.Material.STONE, 1.0), c.blockXp());
        assertTrue(errors.stream().anyMatch(e -> e.contains("NOT_A_BLOCK")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("DIRT")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("SAND")));
    }

    @Test
    void badTierEntryFallsBack() throws Exception {
        RuneConfig c = load("""
                jobs:
                  level-curve:
                    base: 50
                    exponent: 1.3
                  bonemeal-multiplier: 0.25
                  block-xp:
                    STONE: 1
                account:
                  level-curve:
                    base: 200
                    exponent: 1.3
                capacity:
                  rune-cost:
                    base: [1, 2, 3, 5, 8]
                    per-rank: 1
                quests:
                  tier-multipliers: [1, 1.5, 0, 3, 5]
                  tier-min-levels: [1, 1, 1, 15, 25]
                  tier-weights:
                    base: [100, 60, 30, 10, 5]
                    per-level: [-2, 0, 1, 1.5, 1.5]
                  max-reroll-tokens: 3
                tools:
                  speed-by-tier: [4, 6, 8]
                  epic-speed: 12
                  durability-by-tier: [131, 250, 1561]
                  epic-durability: 2031
                  slots-by-rarity: [1, 2, 3, 4, 5]
                  capacity-by-rarity: [3, 6, 10, 14, 18]
                  mastery:
                    pickaxe-actions: 500
                    axe-actions: 1000
                    max-bonus: 0.75
                """);
        assertEquals(1, errors.size());
        assertEquals(RuneConfig.defaults().questTierMultipliers(), c.questTierMultipliers());
    }

    @Test
    void badToolValuesLogAndFallBack() throws Exception {
        RuneConfig c = load("""
                tools:
                  speed-by-tier: [4, 6]
                  epic-speed: banana
                  durability-by-tier: [131, 250.5, 1561]
                  epic-durability: 2031
                  slots-by-rarity: [1, 2, 3, 4, 0]
                  capacity-by-rarity: [3, 6, 10, 14, 18]
                """);
        assertEquals(ToolConfig.defaults(), c.tools());
        assertTrue(errors.stream().anyMatch(e -> e.contains("tools.speed-by-tier")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("tools.epic-speed")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("tools.durability-by-tier")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("tools.slots-by-rarity")));
    }
}
