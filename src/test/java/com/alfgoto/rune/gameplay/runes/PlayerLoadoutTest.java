package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.runes.ToolLoadout.Result;
import com.alfgoto.rune.storage.PdcPlayerStorage;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class PlayerLoadoutTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerStorage storage;
    private ItemRegistry items;
    private RuneRegistry runes;
    private PlayerLoadout loadout;
    private ToolLoadout toolLoadout;
    private PlayerMock player;

    private Rune rune(String id, Rarity rarity, Fit... fits) {
        Effect<?> effect = Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return runes.register(
                new Rune(new RuneItem(id, rarity, Material.PAPER, "test_rune"), effect.with(10, 10), Set.of(fits)));
    }

    private void setAccountLevel(int level) {
        LevelCurve curve = new LevelCurve(200, 1.3);
        storage.set(player.getUniqueId(), "account/xp", Double.toString(curve.totalXpForLevel(level)));
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        storage = new PdcPlayerStorage(plugin);
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        RuneConfig config = RuneConfig.defaults();
        JobProgress progress = new JobProgress(storage, new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        loadout = new PlayerLoadout(storage, runes, new PlayerCapacity(config), progress);
        toolLoadout = new ToolLoadout(plugin, items, runes);
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void capacityFollowsTheAccountLevel() {
        assertEquals(config().forAccountLevel(1), loadout.capacity(player.getUniqueId()));
        setAccountLevel(10);
        assertEquals(15, loadout.capacity(player.getUniqueId()));
        setAccountLevel(100);
        assertEquals(40, loadout.capacity(player.getUniqueId()));
    }

    private PlayerCapacity config() {
        return new PlayerCapacity(RuneConfig.defaults());
    }

    @Test
    void onlyPassiveRunesFitThePlayer() {
        Rune pickOnly = rune("pick_only", Rarity.COMMON, Fit.PICKAXE);
        Rune passive = rune("passive", Rarity.COMMON, Fit.PLAYER);
        setAccountLevel(10);

        assertEquals(Result.DOES_NOT_FIT, loadout.install(player.getUniqueId(), pickOnly, 1));
        assertEquals(Result.INSTALLED, loadout.install(player.getUniqueId(), passive, 1));
    }

    @Test
    void refusesWhenOverCapacityOrOutOfSlots() {
        Rune epic = rune("epic_rune", Rarity.EPIC, Fit.PLAYER); // cost 5
        Rune common = rune("common_rune", Rarity.COMMON, Fit.PLAYER); // cost 1
        setAccountLevel(0); // Capacity 5
        assertEquals(Result.INSTALLED, loadout.install(player.getUniqueId(), epic, 1));
        assertEquals(Result.OVER_CAPACITY, loadout.install(player.getUniqueId(), common, 1));

        setAccountLevel(100); // Capacity 40
        for (int i = 0; i < PlayerLoadout.SLOTS - 1; i++) {
            assertEquals(Result.INSTALLED, loadout.install(player.getUniqueId(), common, 1));
        }
        assertEquals(PlayerLoadout.SLOTS, loadout.installed(player.getUniqueId()).size());
        assertEquals(Result.NO_FREE_SLOT, loadout.install(player.getUniqueId(), common, 1));
    }

    @Test
    void uninstallMovesTheNextRunesUp() {
        Rune a = rune("rune_a", Rarity.COMMON, Fit.PLAYER);
        Rune b = rune("rune_b", Rarity.COMMON, Fit.PLAYER);
        setAccountLevel(10);
        loadout.install(player.getUniqueId(), a, 1);
        loadout.install(player.getUniqueId(), b, 1);

        assertEquals(a, loadout.uninstall(player.getUniqueId(), 1).orElseThrow().rune());
        assertEquals(b, loadout.installed(player.getUniqueId()).get(0).rune());
        assertTrue(loadout.uninstall(player.getUniqueId(), 5).isEmpty());
    }

    @Test
    void playerRunesAreKeptOnDeath() {
        Rune passive = rune("passive", Rarity.RARE, Fit.PLAYER);
        setAccountLevel(10);
        loadout.install(player.getUniqueId(), passive, 1);

        player.damage(1000);
        assertTrue(player.isDead());
        player.respawn();

        assertEquals(1, loadout.installed(player.getUniqueId()).size());
        assertEquals(3, loadout.used(player.getUniqueId()));
    }

    @Test
    void droppedGearKeepsItsInstalledRunes() {
        Rune pickRune = rune("pick_rune", Rarity.COMMON, Fit.PICKAXE);
        ItemStack pick = items.create(
                items.register(RuneItem.tool("rare_pick", Rarity.RARE, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        assertEquals(Result.INSTALLED, toolLoadout.install(pick, pickRune, 1));

        // What a death does: the item is dropped as an entity, then picked up again.
        Item dropped = player.getWorld().dropItem(player.getLocation(), pick);
        ItemStack back = dropped.getItemStack();

        assertEquals(1, toolLoadout.installed(back).size());
        assertEquals(pickRune, toolLoadout.installed(back).get(0).rune());
    }
}
