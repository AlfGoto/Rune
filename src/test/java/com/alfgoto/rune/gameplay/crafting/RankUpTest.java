package com.alfgoto.rune.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

/** The rank-up rules: 3 identical Runes of Rank N, Max Rank, cost x3 per Rank. */
class RankUpTest {

    private RuneRegistry runes;
    private RankUp rankUp;
    private Rune capped;
    private Rune other;

    private static Rune rune(String id, Integer maxRank) {
        Effect<?> effect = Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return new Rune(
                new RuneItem(id, Rarity.RARE, Material.PAPER, "test_rune"),
                maxRank == null ? effect.with(5, 5) : effect.with(5, 5, maxRank),
                Set.of(Fit.PLAYER));
    }

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        ItemRegistry items = new ItemRegistry(plugin);
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        capped = runes.register(rune("capped", 3));
        other = runes.register(rune("other", 3));
        rankUp = new RankUp(runes, RuneConfig.defaults());
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack stack(Rune rune, int rank) {
        return runes.create(rune, rank);
    }

    @Test
    void costIsThreeTimesMoreEachRank() {
        assertEquals(3, rankUp.cost(1));
        assertEquals(9, rankUp.cost(2));
        assertEquals(27, rankUp.cost(3));
    }

    @Test
    void threeIdenticalRunesGiveTheNextRank() {
        RankUp.Plan plan = rankUp.plan(stack(capped, 1), stack(capped, 1), stack(capped, 1));
        assertTrue(plan.valid());
        assertEquals(3, plan.cost());
        assertEquals(2, runes.rankOf(rankUp.result(plan)));
        assertEquals(capped, runes.of(rankUp.result(plan)).orElseThrow());
    }

    @Test
    void refusesAtMaxRank() {
        RankUp.Plan plan = rankUp.plan(stack(capped, 3), stack(capped, 3), stack(capped, 3));
        assertEquals(RankUp.Refusal.MAX_RANK, plan.refusal());
    }

    @Test
    void refusesDifferentRunesOrRanks() {
        assertEquals(
                RankUp.Refusal.DIFFERENT,
                rankUp.plan(stack(capped, 1), stack(other, 1), stack(capped, 1)).refusal());
        assertEquals(
                RankUp.Refusal.DIFFERENT,
                rankUp.plan(stack(capped, 1), stack(capped, 2), stack(capped, 1)).refusal());
    }

    @Test
    void emptySlotOrNonRuneIsNotAnAttempt() {
        assertEquals(
                RankUp.Refusal.NOT_RUNES,
                rankUp.plan(stack(capped, 1), stack(capped, 1), null).refusal());
        assertEquals(
                RankUp.Refusal.NOT_RUNES,
                rankUp.plan(stack(capped, 1), stack(capped, 1), new ItemStack(Material.PAPER)).refusal());
    }

    @Test
    void previewShowsTheCostAndTheMissingLevelsInTheLore() {
        RankUp.Plan plan = rankUp.plan(stack(capped, 1), stack(capped, 1), stack(capped, 1));
        int withEnough = rankUp.preview(plan, 30).lore().size();
        int withTooFew = rankUp.preview(plan, 2).lore().size();
        assertEquals(withEnough + 1, withTooFew);
        assertNull(rankUp.result(plan).lore(), "the real result has no cost lines");
    }
}
