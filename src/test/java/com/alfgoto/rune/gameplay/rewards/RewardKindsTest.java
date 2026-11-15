package com.alfgoto.rune.gameplay.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemReward;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.AccountXpReward;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobXpReward;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.quests.RerollTokenReward;
import com.alfgoto.rune.gameplay.quests.RerollTokens;
import com.alfgoto.rune.storage.PdcPlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.Random;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** Each reward kind: when it is available, what it describes, and what {@code Drop.give} does. */
class RewardKindsTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private PdcPlayerStorage storage;
    private JobProgress progress;
    private final Job mining = Job.builder("mining").accountRate(0.5).build();
    private final Random random = new Random(1);

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        storage = new PdcPlayerStorage(plugin);
        progress = new JobProgress(storage, new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void itemRewardGivesAStackInItsRange() {
        ItemRegistry items = new ItemRegistry(plugin);
        items.register(new RuneItem("shard", Rarity.COMMON, Material.PAPER, "test_rune"));
        ItemReward reward = new ItemReward(items, "shard", 2, 4);
        RollContext context = RollContext.of(player.getUniqueId(), Rarity.COMMON);
        assertTrue(reward.available(context));
        reward.roll(context, random).give(player);
        int count = player.getInventory().getItem(0).getAmount();
        assertTrue(count >= 2 && count <= 4, "count " + count);
        assertFalse(new ItemReward(items, "missing", 1, 1).available(context));
    }

    @Test
    void jobXpRewardNeedsAJob() {
        JobRegistry jobs = new JobRegistry();
        jobs.register(mining);
        JobXpReward reward = new JobXpReward(jobs, progress, 100);
        assertFalse(reward.available(RollContext.of(player.getUniqueId(), Rarity.COMMON)));
        assertFalse(reward.available(new RollContext(player.getUniqueId(), Rarity.COMMON, "unknown")));
        RollContext context = new RollContext(player.getUniqueId(), Rarity.COMMON, "mining");
        assertTrue(reward.available(context));
        reward.roll(context, random).give(player);
        assertEquals(100, progress.jobXp(player.getUniqueId(), mining), 1e-9);
        assertEquals(50, progress.accountXp(player.getUniqueId()), 1e-9); // Account Rate 0.5
    }

    @Test
    void accountXpRewardLeavesJobXpAlone() {
        AccountXpReward reward = new AccountXpReward(progress, 100);
        RollContext context = RollContext.of(player.getUniqueId(), Rarity.COMMON);
        assertTrue(reward.available(context));
        reward.roll(context, random).give(player);
        assertEquals(100, progress.accountXp(player.getUniqueId()), 1e-9);
        assertEquals(0, progress.jobXp(player.getUniqueId(), mining), 1e-9);
    }

    @Test
    void rerollTokenRewardIsCappedAtTheMostHeld() {
        RerollTokens tokens = new RerollTokens(storage, RuneConfig.defaults(), () -> 0L);
        RerollTokenReward reward = new RerollTokenReward(tokens, 2);
        RollContext context = RollContext.of(player.getUniqueId(), Rarity.COMMON);
        reward.roll(context, random).give(player);
        assertEquals(2, tokens.count(player.getUniqueId()));
        reward.roll(context, random).give(player);
        assertEquals(3, tokens.count(player.getUniqueId())); // max-reroll-tokens
        assertEquals(0, tokens.grant(player.getUniqueId(), 1));
    }

    @Test
    void rollingGivesNothingUntilGive() {
        AccountXpReward reward = new AccountXpReward(progress, 100);
        reward.roll(RollContext.of(player.getUniqueId(), Rarity.COMMON), random);
        assertEquals(0, progress.accountXp(player.getUniqueId()), 1e-9);
    }
}
