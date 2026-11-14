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
                account:
                  level-curve:
                    base: 100
                    exponent: 1.1
                quests:
                  tier-multipliers: [1, 2, 3, 4, 6]
                  tier-min-levels: [1, 2, 3, 4, 5]
                  tier-weights:
                    base: [1, 2, 3, 4, 5]
                    per-level: [-1, 0, 1, 2, 3]
                  max-reroll-tokens: 5
                """);
        assertEquals(5, c.questMaxRerollTokens());
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0), c.questTierMinLevels());
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0), c.questTierWeightBase());
        assertEquals(List.of(-1.0, 0.0, 1.0, 2.0, 3.0), c.questTierWeightPerLevel());
        assertEquals(40, c.levelCurveBase());
        assertEquals(1.5, c.levelCurveExponent());
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
        }
    }

    @Test
    void badValuesLogAndFallBack() throws Exception {
        RuneConfig c = load("""
                jobs:
                  level-curve:
                    base: -5
                    exponent: banana
                account:
                  level-curve:
                    base: 200
                    exponent: 1.3
                quests:
                  tier-multipliers: [1, 2]
                  tier-min-levels: [1, 1, 1, 15, 25]
                  tier-weights:
                    base: [100, 60, 30, 10, 5]
                    per-level: [-2, 0, 1, 1.5, 1.5]
                  max-reroll-tokens: 3
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
        assertEquals(9, errors.size());
        assertEquals(3, c.questMaxRerollTokens());
        assertEquals(50, c.levelCurveBase());
        assertEquals(200, c.accountCurveBase());
        assertEquals(1.3, c.accountCurveExponent());
    }

    @Test
    void badTierEntryFallsBack() throws Exception {
        RuneConfig c = load("""
                jobs:
                  level-curve:
                    base: 50
                    exponent: 1.3
                account:
                  level-curve:
                    base: 200
                    exponent: 1.3
                quests:
                  tier-multipliers: [1, 1.5, 0, 3, 5]
                  tier-min-levels: [1, 1, 1, 15, 25]
                  tier-weights:
                    base: [100, 60, 30, 10, 5]
                    per-level: [-2, 0, 1, 1.5, 1.5]
                  max-reroll-tokens: 3
                """);
        assertEquals(1, errors.size());
        assertEquals(RuneConfig.defaults().questTierMultipliers(), c.questTierMultipliers());
    }
}
