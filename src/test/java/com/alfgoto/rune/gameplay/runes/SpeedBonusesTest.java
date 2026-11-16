package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.text.Rarity;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class SpeedBonusesTest {

    private ServerMock server;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private SpeedBonuses speed;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
        speed = new SpeedBonuses(loadout);
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Rune rune(String id, double perRank) {
        Effect<?> effect = Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return runes.register(new Rune(
                new RuneItem(id, Rarity.COMMON, Material.PAPER, "test_rune"),
                effect.with(perRank, perRank, 5),
                Set.of(Fit.PICKAXE)));
    }

    private ItemStack pick() {
        // Legendary: 5 slots, Capacity 18, room for every Rune of the test.
        return items.create(
                items.register(RuneItem.tool("speed_pick", Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
    }

    @Test
    void noRuneMeansNoBonus() {
        speed.register("haste", (p, b) -> true);
        var block = server.addSimpleWorld("w").getBlockAt(0, 64, 0);
        assertEquals(0, speed.percent(pick(), player, block));
    }

    @Test
    void hasteStacksWithRanksAndAcrossRunes() {
        speed.register("haste", (p, b) -> true);
        Rune haste = rune("haste", 8);
        ItemStack pick = pick();
        loadout.install(pick, haste, 1); // +8
        loadout.install(pick, haste, 3); // +24
        var block = server.addSimpleWorld("w2").getBlockAt(0, 64, 0);
        assertEquals(32, speed.percent(pick, player, block));
    }

    @Test
    void aVariantOnlyAppliesWhereItsRuleSays() {
        speed.register("haste", (p, b) -> true);
        speed.register("specialist_iron", (p, b) -> b.getType() == Material.IRON_ORE);
        ItemStack pick = pick();
        loadout.install(pick, rune("haste", 8), 1);
        loadout.install(pick, rune("specialist_iron", 20), 2); // +40
        var world = server.addSimpleWorld("w3");
        var iron = world.getBlockAt(0, 64, 0);
        iron.setType(Material.IRON_ORE);
        var stone = world.getBlockAt(1, 64, 0);
        stone.setType(Material.STONE);

        assertEquals(48, speed.percent(pick, player, iron)); // Haste + Specialist
        assertEquals(8, speed.percent(pick, player, stone)); // Haste only
    }

    @Test
    void listenerPutsTheBonusOnTheBlockBreakSpeedAndReplacesItEachBlock() {
        speed.register("haste", (p, b) -> true);
        speed.register("specialist_iron", (p, b) -> b.getType() == Material.IRON_ORE);
        ItemStack pick = pick();
        loadout.install(pick, rune("haste", 8), 2); // +16
        loadout.install(pick, rune("specialist_iron", 20), 1); // +20
        var world = server.addSimpleWorld("w5");
        var iron = world.getBlockAt(0, 64, 0);
        iron.setType(Material.IRON_ORE);
        var stone = world.getBlockAt(1, 64, 0);
        stone.setType(Material.STONE);
        SpeedListener listener = new SpeedListener(MockBukkit.createMockPlugin(), speed);
        var attribute = player.getAttribute(org.bukkit.attribute.Attribute.BLOCK_BREAK_SPEED);
        double base = attribute.getValue();

        listener.onStartMining(new org.bukkit.event.block.BlockDamageEvent(player, iron, null, pick, false));
        assertEquals(base * 1.36, attribute.getValue(), 1e-6);
        listener.onStartMining(new org.bukkit.event.block.BlockDamageEvent(player, stone, null, pick, false));
        assertEquals(base * 1.16, attribute.getValue(), 1e-6); // replaced, not added
        listener.onStartMining(new org.bukkit.event.block.BlockDamageEvent(
                player, stone, null, new ItemStack(Material.STONE_PICKAXE), false));
        assertEquals(base, attribute.getValue(), 1e-6); // another tool: no bonus
    }

    @Test
    void anUnknownEffectIdIsIgnoredAndADuplicateIsRefused() {
        speed.register("haste", (p, b) -> true);
        assertThrows(IllegalArgumentException.class, () -> speed.register("haste", (p, b) -> true));
        ItemStack pick = pick();
        loadout.install(pick, rune("other", 10), 1); // never registered as a speed effect
        var block = server.addSimpleWorld("w4").getBlockAt(0, 64, 0);
        assertEquals(0, speed.percent(pick, player, block));
    }
}
