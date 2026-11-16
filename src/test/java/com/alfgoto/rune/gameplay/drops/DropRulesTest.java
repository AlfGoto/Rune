package com.alfgoto.rune.gameplay.drops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alfgoto.rune.gameplay.drops.DropRules.Mode;
import com.alfgoto.rune.gameplay.drops.DropRules.Stack;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.Stacking.Source;
import com.alfgoto.rune.gameplay.effects.Trigger;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DropRulesTest {

    // A small furnace: cobblestone → stone → smooth stone, log → charcoal. Anything else stays as it is.
    private static final Map<String, String> FURNACE =
            Map.of("cobblestone", "stone", "stone", "smooth_stone", "log", "charcoal");

    private static String smelt(String item) {
        return FURNACE.getOrDefault(item, item);
    }

    private static Source source(String id, double base, double perRank, int rank) {
        Effect<Object> effect =
                Effect.builder(id, new Trigger<>("test", Object.class, e -> null)).does(ctx -> {});
        return new Source(effect.with(base, perRank), rank);
    }

    private static List<Stack<String>> drop(String item, int amount) {
        return List.of(new Stack<>(item, amount));
    }

    @Test
    void noRuneChangesNothing() {
        assertEquals(Mode.UNCHANGED, DropRules.mode(List.of()));
        assertEquals(Mode.UNCHANGED, DropRules.mode(List.of(source("haste", 8, 8, 1))));
    }

    @Test
    void silkTouchTurnsFortuneAndSmeltOff() {
        List<Source> sources =
                List.of(source("fortune", 15, 15, 3), source("smelt", 25, 25, 3), source("silk_touch", 1, 0, 1));
        assertEquals(Mode.SILK, DropRules.mode(sources));
        // Even if drops were applied, the stacked values are 0 with Silk Touch.
        assertEquals(drop("log", 1), DropRules.apply(sources, drop("log", 1), DropRulesTest::smelt, () -> 0));
    }

    @Test
    void silkTouchAloneIsSilk() {
        assertEquals(Mode.SILK, DropRules.mode(List.of(source("silk_touch", 1, 0, 1))));
    }

    @Test
    void fortuneAndSmeltAreChanged() {
        assertEquals(Mode.CHANGED, DropRules.mode(List.of(source("fortune", 15, 15, 1))));
        assertEquals(Mode.CHANGED, DropRules.mode(List.of(source("smelt", 25, 25, 1))));
    }

    @Test
    void fortuneStacksAndRollsAgainAbove100() {
        // Rank 3 (45%) twice = 90%, plus a 60% Trait = 150%: one sure bonus, 50% for a second.
        List<Source> sources =
                List.of(source("fortune", 15, 15, 3), source("fortune", 15, 15, 3), source("fortune", 60, 0, 1));
        assertEquals(drop("diamond", 3), DropRules.apply(sources, drop("diamond", 1), DropRulesTest::smelt, () -> 0.49));
        assertEquals(drop("diamond", 2), DropRules.apply(sources, drop("diamond", 1), DropRulesTest::smelt, () -> 0.5));
    }

    @Test
    void smelt150GivesStoneThenMaybeSmoothStone() {
        List<Source> sources = List.of(source("smelt", 150, 0, 1));
        assertEquals(drop("smooth_stone", 1), DropRules.apply(sources, drop("cobblestone", 1), DropRulesTest::smelt, () -> 0.49));
        assertEquals(drop("stone", 1), DropRules.apply(sources, drop("cobblestone", 1), DropRulesTest::smelt, () -> 0.5));
    }

    @Test
    void smeltTurnsLogsIntoCharcoalAndStopsThere() {
        List<Source> sources = List.of(source("smelt", 300, 0, 1));
        assertEquals(drop("charcoal", 1), DropRules.apply(sources, drop("log", 1), DropRulesTest::smelt, () -> 0));
    }

    @Test
    void failedSmeltRollKeepsTheDrop() {
        List<Source> sources = List.of(source("smelt", 25, 25, 1));
        assertEquals(drop("cobblestone", 1), DropRules.apply(sources, drop("cobblestone", 1), DropRulesTest::smelt, () -> 0.9));
    }

    @Test
    void fortuneBonusIsSmeltedToo() {
        List<Source> sources = List.of(source("fortune", 100, 0, 1), source("smelt", 100, 0, 1));
        assertEquals(drop("charcoal", 2), DropRules.apply(sources, drop("log", 1), DropRulesTest::smelt, () -> 0));
    }

    @Test
    void emptyDropsStayEmpty() {
        List<Source> sources = List.of(source("fortune", 100, 0, 1));
        assertEquals(List.of(), DropRules.apply(sources, List.<Stack<String>>of(), DropRulesTest::smelt, () -> 0));
    }
}
