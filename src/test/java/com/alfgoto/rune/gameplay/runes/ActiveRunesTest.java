package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.foundations.triggers.ClickType;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.RankValues;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class ActiveRunesTest {

    private ServerMock server;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private ActiveRunes active;
    private final AtomicLong now = new AtomicLong(1_000_000);
    private final AtomicInteger ran = new AtomicInteger();
    private final List<String> heard = new ArrayList<>();

    private Rune activeRune(String id, boolean passes) {
        Effect<RuneClickEvent> effect = Effect.builder(id, EffectTriggers.CLICK)
                .when(context -> passes)
                .does(context -> ran.incrementAndGet());
        return runes.register(Rune.active(
                new RuneItem(id, Rarity.COMMON, Material.PAPER, "test_rune"),
                effect.with(1, 1),
                Set.of(Fit.PICKAXE),
                null,
                RankValues.of(10, -2, 3)));
    }

    private Rune passiveRune(String id) {
        Effect<?> effect = Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return runes.register(new Rune(
                new RuneItem(id, Rarity.COMMON, Material.PAPER, "test_rune"), effect.with(1, 1), Set.of(Fit.PICKAXE)));
    }

    private ItemStack pick() {
        return items.create(
                items.register(RuneItem.tool("epic_pick", Rarity.EPIC, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
    }

    private void click(Player player, ClickType click, ItemStack tool) {
        active.onClick(new RuneClickEvent(player, click, tool));
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
        active = new ActiveRunes(loadout, now::get);
        active.addListener(new ActiveRuneListener() {
            @Override
            public void onUsed(Player player, Rune rune, double seconds) {
                heard.add("used " + rune.id() + " " + seconds);
            }

            @Override
            public void onCooldown(Player player, Rune rune, double secondsLeft) {
                heard.add("cooldown " + rune.id() + " " + secondsLeft);
            }
        });
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void eachClickTriggersItsOwnActiveSlot() {
        Player player = server.addPlayer();
        ItemStack tool = pick();
        Rune right = activeRune("right_rune", true);
        Rune left = activeRune("left_rune", true);
        loadout.install(tool, right, 1); // slot 1 = right-click
        loadout.install(tool, left, 1); // slot 2 = left-click

        click(player, ClickType.RIGHT, tool);
        assertEquals(List.of("used right_rune 10.0"), heard);
        click(player, ClickType.LEFT, tool);
        assertEquals(List.of("used right_rune 10.0", "used left_rune 10.0"), heard);
        assertEquals(2, ran.get());
    }

    @Test
    void cooldownBlocksReuseUntilItEnds() {
        Player player = server.addPlayer();
        ItemStack tool = pick();
        Rune rune = activeRune("cool_rune", true);
        loadout.install(tool, rune, 2); // cooldown 8 s at Rank 2

        click(player, ClickType.RIGHT, tool);
        assertEquals(8.0, active.secondsLeft(player.getUniqueId(), rune));
        now.addAndGet(3_000);
        click(player, ClickType.RIGHT, tool);
        assertEquals(1, ran.get());
        assertEquals("cooldown cool_rune 5.0", heard.get(1));

        now.addAndGet(5_000);
        assertEquals(0, active.secondsLeft(player.getUniqueId(), rune));
        click(player, ClickType.RIGHT, tool);
        assertEquals(2, ran.get());
    }

    @Test
    void cooldownIsPerPlayerAndFollowsTheRuneToAnotherTool() {
        Player one = server.addPlayer();
        Player two = server.addPlayer();
        Rune rune = activeRune("shared_rune", true);
        ItemStack toolA = pick();
        ItemStack toolB = pick();
        loadout.install(toolA, rune, 1);
        loadout.install(toolB, rune, 1);

        click(one, ClickType.RIGHT, toolA);
        click(one, ClickType.RIGHT, toolB);
        assertEquals(1, ran.get());
        click(two, ClickType.RIGHT, toolA);
        assertEquals(2, ran.get());
    }

    @Test
    void onlyActiveSlotsWithActiveRunesAreTriggered() {
        Player player = server.addPlayer();
        ItemStack tool = pick();
        loadout.install(tool, passiveRune("passive"), 1); // slot 1: passive
        loadout.install(tool, activeRune("slot_two", true), 1); // slot 2: left
        loadout.install(tool, activeRune("slot_three", true), 1); // slot 3: no click

        click(player, ClickType.RIGHT, tool);
        assertEquals(0, ran.get());
        click(player, ClickType.LEFT, tool);
        assertEquals(1, ran.get());
        assertTrue(active.inActiveSlot(tool, ClickType.RIGHT).isEmpty());
        assertEquals("slot_two", active.inActiveSlot(tool, ClickType.LEFT).orElseThrow().rune().id());
    }

    @Test
    void aFailedConditionDoesNotStartTheCooldown() {
        Player player = server.addPlayer();
        ItemStack tool = pick();
        Rune rune = activeRune("never", false);
        loadout.install(tool, rune, 1);

        click(player, ClickType.RIGHT, tool);
        assertEquals(0, ran.get());
        assertEquals(0, active.secondsLeft(player.getUniqueId(), rune));
        assertTrue(heard.isEmpty());
    }

    @Test
    void anythingThatIsNotARuneToolIsIgnored() {
        Player player = server.addPlayer();
        click(player, ClickType.RIGHT, new ItemStack(Material.STICK));
        click(player, ClickType.LEFT, new ItemStack(Material.AIR));
        assertTrue(heard.isEmpty());
    }

    @Test
    void aRuneWithAClickEffectMustBeActive() {
        Effect<?> clickEffect = Effect.builder("bad", EffectTriggers.CLICK).does(context -> {});
        RuneItem item = new RuneItem("bad", Rarity.COMMON, Material.PAPER, "test_rune");
        assertThrows(
                IllegalArgumentException.class, () -> new Rune(item, clickEffect.with(1, 1), Set.of(Fit.PICKAXE)));
    }
}
