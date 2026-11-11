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
                quests:
                  tier-multipliers: [1, 2, 3, 4, 6]
                """);
        assertEquals(40, c.levelCurveBase());
        assertEquals(1.5, c.levelCurveExponent());
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
                quests:
                  tier-multipliers: [1, 2]
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
        assertEquals(3, errors.size());
        assertEquals(50, c.levelCurveBase());
    }

    @Test
    void badTierEntryFallsBack() throws Exception {
        RuneConfig c = load("""
                jobs:
                  level-curve:
                    base: 50
                    exponent: 1.3
                quests:
                  tier-multipliers: [1, 1.5, 0, 3, 5]
                """);
        assertEquals(1, errors.size());
        assertEquals(RuneConfig.defaults().questTierMultipliers(), c.questTierMultipliers());
    }
}
