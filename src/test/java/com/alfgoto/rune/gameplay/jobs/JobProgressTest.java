package com.alfgoto.rune.gameplay.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.alfgoto.rune.storage.PlayerStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JobProgressTest {

    private final Job mining = Job.builder("mining").accountRate(1.0).build();
    private final Job woodcutting = Job.builder("woodcutting").accountRate(0.8).build();
    private final UUID player = UUID.randomUUID();
    private final JobProgress progress =
            new JobProgress(new MemoryStorage(), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));

    @Test
    void newPlayerStartsAtLevelOne() {
        assertEquals(0, progress.jobXp(player, mining));
        assertEquals(1, progress.jobLevel(player, mining));
        assertEquals(1, progress.accountLevel(player));
    }

    @Test
    void accountXpIsJobXpTimesAccountRate() {
        progress.addJobXp(player, mining, 100);
        progress.addJobXp(player, woodcutting, 100);
        assertEquals(100, progress.jobXp(player, mining), 1e-9);
        assertEquals(100, progress.jobXp(player, woodcutting), 1e-9);
        assertEquals(180, progress.accountXp(player), 1e-9);
    }

    @Test
    void accountLevelFollowsAccountCurve() {
        progress.addJobXp(player, mining, 200);
        assertEquals(2, progress.accountLevel(player));
    }

    @Test
    void listenersGetTheGainWithLevelsBeforeAndAfter() {
        java.util.List<JobXpGain> gains = new java.util.ArrayList<>();
        progress.addListener(gains::add);
        progress.addJobXp(player, mining, 200);
        assertEquals(1, gains.size());
        JobXpGain gain = gains.get(0);
        assertEquals(200, gain.amount(), 1e-9);
        assertEquals(1, gain.jobLevelBefore());
        assertEquals(true, gain.jobLeveledUp());
        assertEquals(true, gain.accountLeveledUp());
    }

    @Test
    void setLevelSetsMinimumXpForThatLevel() {
        progress.setJobLevel(player, mining, 10);
        assertEquals(10, progress.jobLevel(player, mining));
        assertEquals(progress.jobCurve().totalXpForLevel(10), progress.jobXp(player, mining), 1e-9);
        assertEquals(0, progress.accountXp(player));
    }

    @Test
    void setXpLeavesAccountXpAlone() {
        progress.setJobXp(player, mining, 500);
        assertEquals(500, progress.jobXp(player, mining));
        assertEquals(0, progress.accountXp(player));
    }

    @Test
    void rejectsBadAmounts() {
        assertThrows(IllegalArgumentException.class, () -> progress.addJobXp(player, mining, -1));
        assertThrows(IllegalArgumentException.class, () -> progress.setJobXp(player, mining, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> progress.setJobLevel(player, mining, 0));
    }

    @Test
    void jobBuilderValidates() {
        assertThrows(IllegalArgumentException.class, () -> Job.builder("x").accountRate(0).build());
        assertThrows(IllegalArgumentException.class, () -> Job.builder(" ").build());
        assertEquals("rune.job.mining", mining.translationKey());
    }

    private static final class MemoryStorage implements PlayerStorage {
        private final Map<String, String> data = new HashMap<>();

        @Override
        public Optional<String> get(UUID player, String key) {
            return Optional.ofNullable(data.get(player + key));
        }

        @Override
        public void set(UUID player, String key, String value) {
            data.put(player + key, value);
        }

        @Override
        public void remove(UUID player, String key) {
            data.remove(player + key);
        }
    }
}
