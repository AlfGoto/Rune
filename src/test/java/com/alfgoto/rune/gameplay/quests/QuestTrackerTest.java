package com.alfgoto.rune.gameplay.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.jobs.AccountXpReward;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.rewards.LootRoller;
import com.alfgoto.rune.gameplay.rewards.LootTable;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.storage.PdcPlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.Random;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class QuestTrackerTest {

    private PlayerMock player;
    private JobProgress progress;
    private QuestBook book;
    private QuestTracker tracker;
    private final QuestType mineTest = new QuestType("minetest", CoreJobs.MINING, 3);

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        PdcPlayerStorage storage = new PdcPlayerStorage(plugin);
        progress = new JobProgress(storage, new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        QuestTypeRegistry types = new QuestTypeRegistry();
        types.register(mineTest);
        RuneConfig config = RuneConfig.defaults();
        book = new QuestBook(storage, progress, types.registry(), config, new Random(1), () -> 0);
        LootTableRegistry tables = new LootTableRegistry();
        tables.registry()
                .register(LootTable.builder(QuestTracker.LOOT_TABLE)
                        .add(new AccountXpReward(progress, 1000), 1, Rarity.COMMON)
                        .build());
        tracker = new QuestTracker(book, progress, config, tables, new LootRoller(config), new Random(2));
        book.set(player.getUniqueId(), 1, mineTest, Rarity.COMMON);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void jobActionsOfTheQuestJobAddProgress() {
        tracker.onJobAction(new JobActionEvent(player, CoreJobs.MINING));
        assertEquals(1, book.progress(player.getUniqueId(), 1));
        tracker.onJobAction(new JobActionEvent(player, CoreJobs.WOODCUTTING));
        assertEquals(1, book.progress(player.getUniqueId(), 1), "another Job does not count");
    }

    @Test
    void completionRewardsAndDrawsANewQuest() {
        double jobXpBefore = progress.jobXp(player.getUniqueId(), CoreJobs.MINING);
        double accountBefore = progress.accountXp(player.getUniqueId());
        for (int i = 0; i < mineTest.baseSize(); i++) {
            tracker.onJobAction(new JobActionEvent(player, CoreJobs.MINING));
        }
        assertEquals(0, book.progress(player.getUniqueId(), 1), "the new Quest starts at 0");
        assertEquals(3, book.active(player.getUniqueId()).size());
        assertTrue(progress.jobXp(player.getUniqueId(), CoreJobs.MINING) >= jobXpBefore + 50);
        assertTrue(progress.accountXp(player.getUniqueId()) >= accountBefore + 25 + 1000, "bonus + loot roll");
    }
}
