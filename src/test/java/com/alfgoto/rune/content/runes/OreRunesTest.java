package com.alfgoto.rune.content.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.SpeedBonuses;
import com.alfgoto.rune.gameplay.runes.StoneFamily;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** Deepstride and Ore Bloom (Ore Sense is tested in visuals.reveal). */
class OreRunesTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private BlockTracker tracker;
    private int picks;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        // The player joins before the plugin loads: MockBukkit has no getVirtualHost for the pack join listener.
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("ore");
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        tracker = plugin.context().get(BlockTracker.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack pick(String runeId, int rank) {
        ItemStack pick = items.create(
                items.register(RuneItem.tool(
                        "ore_pick_" + picks++, Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(pick, runes.get(runeId), rank));
        return pick;
    }

    private OreBloomListener bloom(double random) {
        return new OreBloomListener(items, loadout, tracker, () -> random);
    }

    private void mine(OreBloomListener listener, Block block, ItemStack tool) {
        listener.onBlockBreak(new RuneBlockBreakEvent(player, block, BreakCause.TOOL, tool));
    }

    @Test
    void theThreeRunesAreRegisteredWithTheDocsValues() {
        var sense = runes.get("ore_sense");
        var bloom = runes.get("ore_bloom");
        var deep = runes.get("deepstride");
        assertEquals(Rarity.UNCOMMON, sense.item().rarity());
        assertEquals(Rarity.EPIC, bloom.item().rarity());
        assertEquals(Rarity.UNCOMMON, deep.item().rarity());
        assertEquals(15, sense.effect().valueAt(3)); // +5% per Rank
        assertEquals(40, bloom.effect().valueAt(5)); // +8% per Rank
        assertTrue(deep.effect().values().maxRank().isEmpty()); // no Max Rank
        assertEquals(0, deep.effect().valueAt(1)); // Rank 1: below Y 0
        assertEquals(16, deep.effect().valueAt(2)); // each Rank moves the line up 16
    }

    @Test
    void deepstrideGivesSpeedOnlyBelowItsLine() {
        SpeedBonuses speed = plugin.context().get(SpeedBonuses.class);
        Block block = world.getBlockAt(0, 10, 0);
        ItemStack rank1 = pick("deepstride", 1);
        player.teleport(new Location(world, 0, -5, 0));
        assertEquals(20, speed.percent(rank1, player, block));
        player.teleport(new Location(world, 0, 5, 0));
        assertEquals(0, speed.percent(rank1, player, block)); // above Y 0

        ItemStack rank2 = pick("deepstride", 2); // line moved up to Y 16
        assertEquals(20, speed.percent(rank2, player, block));
        player.teleport(new Location(world, 0, 20, 0));
        assertEquals(0, speed.percent(rank2, player, block));
    }

    @Test
    void anOreBloomsIntoOneNeighbourStone() {
        Block ore = world.getBlockAt(0, 10, 0);
        ore.setType(Material.IRON_ORE);
        Block stone = world.getBlockAt(1, 10, 0);
        stone.setType(Material.STONE);
        mine(bloom(0), ore, pick("ore_bloom", 1));
        assertEquals(Material.IRON_ORE, stone.getType());
        assertTrue(tracker.tags(stone).isEmpty()); // not placed: full XP
    }

    @Test
    void deepslateBecomesTheDeepslateOreAndNetherrackTheNetherOre() {
        Block ore = world.getBlockAt(0, 10, 0);
        ore.setType(Material.IRON_ORE);
        Block deepslate = world.getBlockAt(0, 9, 0);
        deepslate.setType(Material.DEEPSLATE);
        mine(bloom(0), ore, pick("ore_bloom", 1));
        assertEquals(Material.DEEPSLATE_IRON_ORE, deepslate.getType());

        Block nether = world.getBlockAt(10, 10, 0);
        nether.setType(Material.NETHER_GOLD_ORE);
        Block rack = world.getBlockAt(11, 10, 0);
        rack.setType(Material.NETHERRACK);
        mine(bloom(0), nether, pick("ore_bloom", 2));
        assertEquals(Material.NETHER_GOLD_ORE, rack.getType());
    }

    @Test
    void noBloomWhenTheChanceFailsOnSilkTouchOrWithoutNeighbours() {
        Block ore = world.getBlockAt(0, 10, 0);
        ore.setType(Material.COAL_ORE);
        Block stone = world.getBlockAt(1, 10, 0);
        stone.setType(Material.STONE);
        mine(bloom(0.99), ore, pick("ore_bloom", 1)); // 8% chance, roll 99
        assertEquals(Material.STONE, stone.getType());

        ItemStack silk = pick("ore_bloom", 1);
        loadout.install(silk, runes.get("silk_touch"), 1);
        mine(bloom(0), ore, silk);
        assertEquals(Material.STONE, stone.getType());

        stone.setType(Material.DIRT);
        mine(bloom(0), ore, pick("ore_bloom", 1)); // nothing to bloom into: no error
        assertEquals(Material.DIRT, stone.getType());
    }

    @Test
    void placedStoneAndNonRuneToolsDoNotBloom() {
        Block ore = world.getBlockAt(0, 10, 0);
        ore.setType(Material.GOLD_ORE);
        Block stone = world.getBlockAt(1, 10, 0);
        stone.setType(Material.STONE);
        tracker.add(stone, BlockTag.PLACED);
        mine(bloom(0), ore, pick("ore_bloom", 1));
        assertEquals(Material.STONE, stone.getType());

        tracker.remove(stone, BlockTag.PLACED);
        mine(bloom(0), ore, new ItemStack(Material.IRON_PICKAXE));
        assertEquals(Material.STONE, stone.getType());
    }

    @Test
    void stoneFamilyRules() {
        assertTrue(StoneFamily.isStone(Material.TUFF));
        assertTrue(StoneFamily.isStone(Material.NETHERRACK));
        assertFalse(StoneFamily.isStone(Material.DIRT));
        assertEquals(Optional.empty(), StoneFamily.bloomInto(Material.IRON_ORE, Material.NETHERRACK));
        assertEquals(Optional.empty(), StoneFamily.bloomInto(Material.NETHER_GOLD_ORE, Material.STONE));
        assertEquals(Optional.empty(), StoneFamily.bloomInto(Material.ANCIENT_DEBRIS, Material.STONE));
        assertEquals(
                Optional.of(Material.DIAMOND_ORE), StoneFamily.bloomInto(Material.DEEPSLATE_DIAMOND_ORE, Material.GRANITE));
        assertEquals(
                Optional.of(Material.DEEPSLATE_DIAMOND_ORE),
                StoneFamily.bloomInto(Material.DIAMOND_ORE, Material.DEEPSLATE));
    }
}
