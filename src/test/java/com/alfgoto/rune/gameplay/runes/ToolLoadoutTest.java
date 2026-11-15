package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.ToolLoadout.Result;
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

class ToolLoadoutTest {

    private ServerMock server;
    private RunePlugin plugin;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;

    private Rune rune(String id, String effectId, Rarity rarity, Integer maxRank, Fit... fits) {
        Effect<?> effect = Effect.builder(effectId, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return runes.register(new Rune(
                new RuneItem(id, rarity, Material.PAPER, "test_rune"),
                maxRank == null ? effect.with(10, 10) : effect.with(10, 10, maxRank),
                Set.of(fits)));
    }

    private ItemStack tool(String id, Rarity rarity, ToolKind kind) {
        return items.create(items.register(RuneItem.tool(id, rarity, kind, "test_rune", ToolSpec.DEFAULT)), 1);
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void installsAndUninstallsAndKeepsRunesOnTheTool() {
        Rune speed = rune("speed", "speed", Rarity.COMMON, null, Fit.PICKAXE);
        ItemStack pick = tool("rare_pick", Rarity.RARE, ToolKind.PICKAXE); // 3 slots, Capacity 10

        assertEquals(Result.INSTALLED, loadout.install(pick, speed, 1));
        assertEquals(Result.INSTALLED, loadout.install(pick, speed, 3)); // cost 3
        assertEquals(2, loadout.installed(pick).size());
        assertEquals(1 + 3, loadout.used(pick));
        assertEquals(10, loadout.capacity(pick));

        ItemStack copy = pick.clone(); // the data is on the stack itself
        assertEquals(2, loadout.installed(copy).size());
        assertEquals(3, loadout.installed(copy).get(1).rank());

        assertEquals(1, loadout.uninstall(pick, 1).orElseThrow().rank());
        assertEquals(1, loadout.installed(pick).size());
        assertEquals(3, loadout.installed(pick).get(0).rank()); // moved up
        assertTrue(loadout.uninstall(pick, 2).isEmpty());
        assertTrue(loadout.uninstall(pick, 0).isEmpty());
        assertTrue(loadout.uninstall(pick, 1).isPresent());
        assertEquals(0, loadout.used(pick));
    }

    @Test
    void rejectsARuneThatDoesNotFit() {
        Rune playerOnly = rune("player_only", "x", Rarity.COMMON, null, Fit.PLAYER);
        Rune axeOnly = rune("axe_only", "y", Rarity.COMMON, null, Fit.AXE);
        ItemStack pick = tool("fit_pick", Rarity.RARE, ToolKind.PICKAXE);

        assertEquals(Result.DOES_NOT_FIT, loadout.install(pick, playerOnly, 1));
        assertEquals(Result.DOES_NOT_FIT, loadout.install(pick, axeOnly, 1));
        assertTrue(loadout.installed(pick).isEmpty());
    }

    @Test
    void rejectsWhenCapacityWouldBeExceeded() {
        Rune epic = rune("epic_rune", "e", Rarity.EPIC, null, Fit.PICKAXE); // cost 5
        ItemStack pick = tool("uncommon_pick", Rarity.UNCOMMON, ToolKind.PICKAXE); // 2 slots, Capacity 6

        assertEquals(Result.INSTALLED, loadout.install(pick, epic, 1)); // 5 of 6
        assertEquals(Result.OVER_CAPACITY, loadout.install(pick, epic, 1));
        assertEquals(1, loadout.installed(pick).size());
        // A Rune that costs more than the whole Capacity never fits (Common: Capacity 3).
        ItemStack common = tool("common_pick_cap", Rarity.COMMON, ToolKind.PICKAXE);
        assertEquals(Result.OVER_CAPACITY, loadout.install(common, epic, 1));
    }

    @Test
    void rejectsWhenNoSlotIsFree() {
        Rune cheap = rune("cheap", "c", Rarity.COMMON, null, Fit.PICKAXE); // cost 1
        ItemStack pick = tool("common_pick", Rarity.COMMON, ToolKind.PICKAXE); // 1 slot, Capacity 3

        assertEquals(Result.INSTALLED, loadout.install(pick, cheap, 1));
        assertEquals(Result.NO_FREE_SLOT, loadout.install(pick, cheap, 1));
    }

    @Test
    void rejectsABadRankAndANonTool() {
        Rune capped = rune("capped", "k", Rarity.COMMON, 2, Fit.PICKAXE);
        ItemStack pick = tool("rank_pick", Rarity.RARE, ToolKind.PICKAXE);

        assertEquals(Result.BAD_RANK, loadout.install(pick, capped, 3));
        assertEquals(Result.BAD_RANK, loadout.install(pick, capped, 0));
        assertEquals(Result.NOT_A_TOOL, loadout.install(new ItemStack(Material.WOODEN_PICKAXE), capped, 1));
        assertEquals(Result.NOT_A_TOOL, loadout.install(new ItemStack(Material.AIR), capped, 1));
    }

    @Test
    void effectTotalsStackWithRanksAndSilkTouchTurnsOffSmelt() {
        Rune smelt = rune("smelt_rune", "smelt", Rarity.COMMON, null, Fit.PICKAXE); // 10 + 10 per Rank above 1
        Rune silk = rune("silk_rune", "silk_touch", Rarity.COMMON, 1, Fit.PICKAXE);
        ItemStack pick = tool("total_pick", Rarity.RARE, ToolKind.PICKAXE);

        assertEquals(0, loadout.total(pick, "smelt"));
        loadout.install(pick, smelt, 1); // 10
        loadout.install(pick, smelt, 3); // 30
        assertEquals(40, loadout.total(pick, "smelt"));
        assertEquals(0, loadout.total(pick, "fortune"));

        loadout.install(pick, silk, 1);
        assertEquals(0, loadout.total(pick, "smelt")); // Silk Touch turns it off
        assertEquals(10, loadout.total(pick, "silk_touch"));
        assertEquals(3, loadout.sources(pick).size());
    }

    @Test
    void debugCommandsInstallAndUninstallOnTheHeldTool() {
        Rune speed = rune("speed_cmd", "speed", Rarity.COMMON, null, Fit.PICKAXE);
        PlayerMock player = server.addPlayer();
        player.addAttachment(plugin, RuneCommand.PERMISSION, true);
        ItemStack pick = tool("cmd_pick", Rarity.COMMON, ToolKind.PICKAXE); // 1 slot, Capacity 3
        player.getInventory().setItemInMainHand(pick);

        var install = LoadoutSubcommands.install(loadout, runes);
        var uninstall = LoadoutSubcommands.uninstall(loadout);

        install.execute(player, java.util.List.of("speed_cmd", "2"));
        assertEquals(1, loadout.installed(player.getInventory().getItemInMainHand()).size());
        assertEquals(2, loadout.used(player.getInventory().getItemInMainHand()));

        install.execute(player, java.util.List.of("speed_cmd")); // no free slot
        assertEquals(1, loadout.installed(player.getInventory().getItemInMainHand()).size());

        uninstall.execute(player, java.util.List.of("1"));
        assertTrue(loadout.installed(player.getInventory().getItemInMainHand()).isEmpty());
        assertEquals(speed.id(), "speed_cmd");
    }
}
