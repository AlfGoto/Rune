package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Rarity;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class RuneRewardTest {

    private static final Job MINING = Job.builder("mining").build();
    private static final Job WOODCUTTING = Job.builder("woodcutting").build();

    private ServerMock server;
    private RuneRegistry runes;
    private final RuneConfig config = RuneConfig.defaults();
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        runes = new RuneRegistry(plugin, new ItemRegistry(plugin), new CapacityCost(config));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void add(String id, Rarity rarity, Job job) {
        Effect<?> effect = Effect.builder("fx_" + id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        runes.register(new Rune(
                new RuneItem(id, rarity, Material.PAPER, "test_rune"), effect.with(5, 5), Set.of(Fit.PLAYER), job));
    }

    private String rolled(RuneReward reward, RollContext context, Random random) {
        PlayerMock player = server.addPlayer();
        Drop drop = reward.roll(context, random);
        drop.give(player);
        ItemStack stack = player.getInventory().getItem(0);
        return runes.of(stack).orElseThrow().id();
    }

    @Test
    void dropGivesTheRuneAtRankOne() {
        add("general", Rarity.RARE, null);
        PlayerMock player = server.addPlayer();
        Drop drop = new RuneReward(runes, config, Rarity.RARE).roll(RollContext.of(id, Rarity.COMMON), new Random(1));
        drop.give(player);
        ItemStack stack = player.getInventory().getItem(0);
        assertEquals("general", runes.of(stack).orElseThrow().id());
        assertEquals(1, runes.rankOf(stack));
    }

    @Test
    void aJobLeansTowardsItsRunesSixtyPercentOfTheTime() {
        add("general", Rarity.RARE, null);
        add("mining_rune", Rarity.RARE, MINING);
        add("wood_rune", Rarity.RARE, WOODCUTTING);
        RuneReward reward = new RuneReward(runes, config, Rarity.RARE);
        RollContext context = new RollContext(id, Rarity.COMMON, "mining");
        Random random = new Random(5);
        int mining = 0;
        int n = 10_000;
        for (int i = 0; i < n; i++) {
            String picked = reward.roll(context, random).describe().toString();
            assertFalse(picked.contains("wood_rune"), "another Job's Rune must never drop");
            if (picked.contains("mining_rune")) {
                mining++;
            }
        }
        assertEquals(0.6, mining / (double) n, 0.02);
    }

    @Test
    void withoutAJobOnlyGeneralRunesDrop() {
        add("general", Rarity.RARE, null);
        add("mining_rune", Rarity.RARE, MINING);
        RuneReward reward = new RuneReward(runes, config, Rarity.RARE);
        Random random = new Random(2);
        for (int i = 0; i < 200; i++) {
            assertEquals("general", rolled(reward, RollContext.of(id, Rarity.COMMON), random));
        }
    }

    @Test
    void anEmptyPoolFallsBackToTheOtherPool() {
        add("general_epic", Rarity.EPIC, null);
        add("mining_rare", Rarity.RARE, MINING);
        Random random = new Random(3);
        RollContext mining = new RollContext(id, Rarity.COMMON, "mining");
        // The Job pool has no Epic: always the general Epic.
        RuneReward epic = new RuneReward(runes, config, Rarity.EPIC);
        for (int i = 0; i < 100; i++) {
            assertEquals("general_epic", rolled(epic, mining, random));
        }
        // The general pool has no Rare: always the Job's Rare, whatever the bias.
        RuneReward rare = new RuneReward(runes, config, Rarity.RARE);
        for (int i = 0; i < 100; i++) {
            assertEquals("mining_rare", rolled(rare, mining, random));
        }
    }

    @Test
    void whenBothPoolsAreEmptyItStepsDownOneRarity() {
        add("common_rune", Rarity.COMMON, null);
        add("rare_rune", Rarity.RARE, null);
        RollContext context = RollContext.of(id, Rarity.COMMON);
        Random random = new Random(4);
        // Legendary and Epic have no Rune: steps down to Rare, not to Common.
        assertEquals("rare_rune", rolled(new RuneReward(runes, config, Rarity.LEGENDARY), context, random));
        // Uncommon has no Rune: steps down to Common.
        assertEquals("common_rune", rolled(new RuneReward(runes, config, Rarity.UNCOMMON), context, random));
    }

    @Test
    void notAvailableWhenNoRuneIsFoundAtAnyRarity() {
        RuneReward reward = new RuneReward(runes, config, Rarity.LEGENDARY);
        assertFalse(reward.available(RollContext.of(id, Rarity.COMMON)));
        add("epic_rune", Rarity.EPIC, null);
        assertTrue(reward.available(RollContext.of(id, Rarity.COMMON)));
    }

    @Test
    void aJobRuneOnlyCountsWithThatJobInTheContext() {
        add("mining_legendary", Rarity.LEGENDARY, MINING);
        RuneReward reward = new RuneReward(runes, config, Rarity.LEGENDARY);
        assertFalse(reward.available(RollContext.of(id, Rarity.COMMON)));
        assertFalse(reward.available(new RollContext(id, Rarity.COMMON, "woodcutting")));
        assertTrue(reward.available(new RollContext(id, Rarity.COMMON, "mining")));
    }
}
