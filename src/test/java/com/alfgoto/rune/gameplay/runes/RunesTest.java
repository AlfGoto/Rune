package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectBinding;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class RunesTest {

    private ServerMock server;
    private RuneRegistry runes;
    private GiveSubcommand give;
    private PlayerMock player;

    private static EffectBinding<?> binding(Integer maxRank) {
        Effect<?> effect = Effect.builder("test_effect", EffectTriggers.BLOCK_BREAK).does(context -> {});
        return maxRank == null ? effect.with(5, 5) : effect.with(5, 5, maxRank);
    }

    private static Rune rune(String id, Rarity rarity, Integer maxRank) {
        return new Rune(
                new RuneItem(id, rarity, Material.PAPER, "test_rune"), binding(maxRank), Set.of(Fit.PLAYER));
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        ItemRegistry items = new ItemRegistry(plugin);
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        runes.register(rune("capped", Rarity.RARE, 3));
        runes.register(rune("endless", Rarity.COMMON, null));
        give = new GiveSubcommand(items);
        give.addVariant(new GiveRuneVariant(runes));
        player = server.addPlayer();
        player.addAttachment(plugin, RuneCommand.PERMISSION, true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void capacityCostIsRarityBasePlusOnePerRankAboveOne() {
        CapacityCost cost = new CapacityCost(RuneConfig.defaults());
        assertEquals(1, cost.of(Rarity.COMMON, 1));
        assertEquals(2, cost.of(Rarity.UNCOMMON, 1));
        assertEquals(3, cost.of(Rarity.RARE, 1));
        assertEquals(5, cost.of(Rarity.EPIC, 1));
        assertEquals(8, cost.of(Rarity.LEGENDARY, 1));
        assertEquals(2, cost.of(Rarity.COMMON, 2));
        assertEquals(5, cost.of(Rarity.RARE, 3));
        assertEquals(17, cost.of(Rarity.LEGENDARY, 10));
        assertThrows(IllegalArgumentException.class, () -> cost.of(Rarity.COMMON, 0));
    }

    @Test
    void maxRankIsCheckedButOptional() {
        assertEquals(3, runes.get("capped").maxRank().getAsInt());
        assertTrue(runes.get("capped").allowsRank(3));
        assertFalse(runes.get("capped").allowsRank(4));
        assertFalse(runes.get("capped").allowsRank(0));
        assertTrue(runes.get("endless").maxRank().isEmpty());
        assertTrue(runes.get("endless").allowsRank(500));
    }

    @Test
    void runeMustFitSomewhere() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Rune(new RuneItem("x", Rarity.COMMON, Material.PAPER, "m"), binding(null), Set.of()));
    }

    @Test
    void createdStackKeepsItsRuneAndRank() {
        ItemStack stack = runes.create(runes.get("capped"), 2);
        ItemStack loaded = ItemStack.deserializeBytes(stack.serializeAsBytes());
        assertEquals("capped", runes.of(loaded).orElseThrow().id());
        assertEquals(2, runes.rankOf(loaded));
        assertEquals(1, runes.rankOf(new ItemStack(Material.PAPER)));
        assertThrows(IllegalArgumentException.class, () -> runes.create(runes.get("capped"), 4));
    }

    @Test
    void giveRuneGivesTheRuneAtTheRank() {
        give.execute(player, List.of("rune", "capped", "2"));
        ItemStack stack = player.getInventory().getItem(0);
        assertEquals("capped", runes.of(stack).orElseThrow().id());
        assertEquals(2, runes.rankOf(stack));
    }

    @Test
    void giveRuneDefaultsToRankOne() {
        give.execute(player, List.of("rune", "endless"));
        assertEquals(1, runes.rankOf(player.getInventory().getItem(0)));
    }

    @Test
    void giveRuneRefusesBadInput() {
        give.execute(player, List.of("rune", "capped", "4")); // above Max Rank
        give.execute(player, List.of("rune", "capped", "0"));
        give.execute(player, List.of("rune", "capped", "abc"));
        give.execute(player, List.of("rune", "nope"));
        give.execute(player, List.of("rune"));
        assertEquals(0, player.getInventory().all(Material.PAPER).size());
    }

    @Test
    void giveCompletesRuneKeywordAndIds() {
        assertTrue(give.complete(player, List.of("ru")).contains("rune"));
        assertEquals(List.of("capped"), give.complete(player, List.of("rune", "ca")));
    }
}
