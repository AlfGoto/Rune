package com.alfgoto.rune.gameplay.durability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class SturdyTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void noPercentKeepsTheDamage() {
        assertEquals(10, Sturdy.damage(10, 0, () -> 0.0));
        assertEquals(0, Sturdy.damage(0, 50, () -> 0.0));
    }

    @Test
    void eachPointIsSparedWithTheChance() {
        assertEquals(0, Sturdy.damage(10, 25, () -> 0.1)); // 10 < 25: every point spared
        assertEquals(10, Sturdy.damage(10, 25, () -> 0.9)); // 90 >= 25: none spared
    }

    @Test
    void aHundredPercentSparesEverything() {
        assertEquals(0, Sturdy.damage(50, 100, () -> 0.999));
        assertEquals(0, Sturdy.damage(50, 250, () -> 0.999)); // capped
    }

    @Test
    void roughlyAQuarterLessOverManyUses() {
        java.util.Random random = new java.util.Random(7);
        int lost = Sturdy.damage(100_000, 25, random::nextDouble);
        assertTrue(Math.abs(lost - 75_000) < 1_500, "lost " + lost);
    }

    @Test
    void theRuneHasTheDocsValues() {
        var sturdy = runes.get("sturdy");
        assertEquals(Rarity.COMMON, sturdy.item().rarity());
        assertEquals(25, sturdy.effect().valueAt(1));
        assertEquals(100, sturdy.effect().valueAt(4)); // Max Rank 4: -25% per Rank
        assertEquals(4, sturdy.effect().values().maxRank().orElseThrow());
    }

    private ItemStack tool(int rank) {
        ItemStack pick = items.create(
                items.register(RuneItem.tool(
                        "sturdy_pick_" + rank, Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        if (rank > 0) {
            assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(pick, runes.get("sturdy"), rank));
        }
        return pick;
    }

    @Test
    @SuppressWarnings("deprecation")
    void rankFourToolLosesNoDurabilityAndPlainToolLosesAll() {
        PlayerItemDamageEvent plain = new PlayerItemDamageEvent(player, tool(0), 20);
        server.getPluginManager().callEvent(plain);
        assertEquals(20, plain.getDamage());
        assertTrue(!plain.isCancelled());

        PlayerItemDamageEvent sturdy = new PlayerItemDamageEvent(player, tool(4), 20);
        server.getPluginManager().callEvent(sturdy);
        assertTrue(sturdy.isCancelled()); // 100%: nothing is lost
    }

    @Test
    @SuppressWarnings("deprecation")
    void rankOneToolLosesLessOnAverage() {
        ItemStack pick = tool(1);
        int total = 0;
        for (int i = 0; i < 400; i++) {
            PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, pick, 10);
            server.getPluginManager().callEvent(event);
            total += event.isCancelled() ? 0 : event.getDamage();
        }
        assertTrue(total < 4000 * 0.85 && total > 4000 * 0.65, "lost " + total + " of 4000");
    }
}
