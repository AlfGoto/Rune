package com.alfgoto.rune.gameplay.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.registry.DuplicateIdException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EffectTest {

    /** A test event with no Bukkit parts. The player is null: tests only look at the other fields. */
    record Ping(String tag) {}

    private static final Trigger<Ping> PING = new Trigger<>("ping", Ping.class, p -> null);

    private final List<EffectContext<Ping>> seen = new ArrayList<>();

    private Effect<Ping> recorder(String id) {
        return Effect.builder(id, PING).does(seen::add);
    }

    @Test
    void runsActionWithEventRankAndValue() {
        EffectBinding<Ping> binding = recorder("rec").with(8, 8);
        Ping event = new Ping("a");

        assertTrue(binding.run(event, 2));

        assertEquals(1, seen.size());
        assertSame(event, seen.get(0).event());
        assertEquals(2, seen.get(0).rank());
        assertEquals(16, seen.get(0).value());
    }

    @Test
    void valueFollowsRank() {
        EffectBinding<Ping> binding = recorder("rec").with(8, 8);
        binding.run(new Ping("a"), 1);
        binding.run(new Ping("a"), 3);
        assertEquals(8, seen.get(0).value());
        assertEquals(24, seen.get(1).value());
        assertEquals(24, binding.valueAt(3));
    }

    @Test
    void sameEffectCanHaveDifferentValuesPerBinding() {
        Effect<Ping> effect = recorder("rec");
        effect.with(1, 1).run(new Ping("a"), 2);
        effect.with(10, 10).run(new Ping("a"), 2);
        assertEquals(2, seen.get(0).value());
        assertEquals(20, seen.get(1).value());
    }

    @Test
    void failingConditionStopsTheAction() {
        Effect<Ping> effect = Effect.builder("cond", PING)
                .when(ctx -> ctx.event().tag().equals("yes"))
                .does(seen::add);
        EffectBinding<Ping> binding = effect.with(1, 0);

        assertFalse(binding.run(new Ping("no"), 1));
        assertTrue(seen.isEmpty());
        assertTrue(binding.run(new Ping("yes"), 1));
        assertEquals(1, seen.size());
    }

    @Test
    void allConditionsMustPass() {
        List<String> checked = new ArrayList<>();
        Effect<Ping> effect = Effect.builder("cond", PING)
                .when(ctx -> {
                    checked.add("first");
                    return false;
                })
                .when(ctx -> {
                    checked.add("second");
                    return true;
                })
                .does(seen::add);

        assertFalse(effect.with(1, 0).run(new Ping("a"), 1));
        assertEquals(List.of("first"), checked);
        assertTrue(seen.isEmpty());
    }

    @Test
    void conditionsCanReadTheValue() {
        Effect<Ping> effect = Effect.builder("cond", PING)
                .when(ctx -> ctx.value() > 5)
                .does(seen::add);
        EffectBinding<Ping> binding = effect.with(1, 10);

        assertFalse(binding.run(new Ping("a"), 1));
        assertTrue(binding.run(new Ping("a"), 2));
    }

    @Test
    void rankAboveMaxRankDoesNotThrowAndUsesTheMaxRankValue() {
        EffectBinding<Ping> binding = recorder("rec").with(1, 1, 2);
        assertTrue(binding.run(new Ping("a"), 3));
        assertEquals(2, seen.get(0).value());
    }

    @Test
    void triggerFindsThePlayer() {
        Trigger<Ping> trigger = new Trigger<>("p", Ping.class, p -> null);
        assertEquals("p", trigger.id());
        assertEquals(Ping.class, trigger.eventType());
        assertEquals(null, trigger.playerOf(new Ping("a")));
    }

    @Test
    void exposesIdTriggerAndValues() {
        Effect<Ping> effect = recorder("rec");
        EffectBinding<Ping> binding = effect.with(RankValues.of(1, 2, 4));
        assertEquals("rec", effect.id());
        assertSame(PING, effect.trigger());
        assertSame(effect, binding.effect());
        assertEquals(4, binding.values().maxRank().getAsInt());
    }

    @Test
    void rejectsBadDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> Effect.builder(" ", PING));
        assertThrows(NullPointerException.class, () -> Effect.builder("x", null));
        assertThrows(NullPointerException.class, () -> Effect.builder("x", PING).does(null));
        assertThrows(NullPointerException.class, () -> Effect.builder("x", PING).when(null));
        assertThrows(IllegalArgumentException.class, () -> new Trigger<>("", Ping.class, p -> null));
        assertThrows(NullPointerException.class, () -> recorder("rec").with(1, 0).run(null, 1));
    }

    @Test
    void builtInTriggersHaveUniqueIds() {
        List<String> ids = List.of(EffectTriggers.BLOCK_BREAK.id(), EffectTriggers.CLICK.id());
        assertEquals(ids.size(), Set.copyOf(ids).size());
    }

    @Test
    void registryHoldsEffectsById() {
        EffectRegistry registry = new EffectRegistry();
        Effect<Ping> a = registry.register(recorder("a"));
        registry.register(recorder("b"));

        assertSame(a, registry.get("a"));
        assertTrue(registry.find("b").isPresent());
        assertTrue(registry.find("c").isEmpty());
        assertEquals(2, registry.all().size());
        assertThrows(IllegalArgumentException.class, () -> registry.get("c"));
        assertThrows(DuplicateIdException.class, () -> registry.register(recorder("a")));
    }
}
