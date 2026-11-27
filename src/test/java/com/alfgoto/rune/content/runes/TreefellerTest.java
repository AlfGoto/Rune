package com.alfgoto.rune.content.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.trees.TreeScan;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

/** Treefeller: the cap per Rank (canopy included), at-or-above, same wood type, and the Effect breaks. */
class TreefellerTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private BlockTracker tracker;
    private final List<Block> broken = new ArrayList<>();
    private int axes;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("fell");
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        tracker = plugin.context().get(BlockTracker.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack axe(int rank) {
        ItemStack axe = items.create(
                items.register(RuneItem.tool(
                        "fell_axe_" + axes++, Rarity.EPIC, ToolKind.AXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(axe, runes.get("treefeller"), rank));
        return axe;
    }

    private TreefellerListener listener() {
        MultiBreak multi = new MultiBreak(plugin.context().get(Triggers.class), (p, block) -> {
            broken.add(block);
            block.setType(Material.AIR);
            return true;
        });
        return new TreefellerListener(
                items, loadout, tracker, plugin.context().get(TreeScan.class), multi, stack -> false);
    }

    private void chop(Block block, ItemStack tool, BreakCause cause) {
        player.getInventory().setItemInMainHand(tool);
        listener().onBlockBreak(new RuneBlockBreakEvent(player, block, cause, tool));
    }

    /** A trunk of {@code height} logs from y 10 up. */
    private void trunk(int height, Material log) {
        for (int y = 10; y < 10 + height; y++) {
            world.getBlockAt(0, y, 0).setType(log);
        }
    }

    @Test
    void theRuneIsEpicAxeOnlyAndCapsAre16_32_64() {
        var rune = runes.get("treefeller");
        assertEquals(Rarity.EPIC, rune.item().rarity());
        assertEquals(Set.of(Fit.AXE), rune.fits());
        assertEquals(16, rune.effect().valueAt(1));
        assertEquals(32, rune.effect().valueAt(2));
        assertEquals(64, rune.effect().valueAt(3));
        assertEquals(3, rune.maxRank().getAsInt());
    }

    @Test
    void theCapCountsTheFirstBlock() {
        trunk(100, Material.OAK_LOG);
        chop(world.getBlockAt(0, 10, 0), axe(1), BreakCause.TOOL);
        assertEquals(15, broken.size());
        broken.clear();
        trunk(100, Material.OAK_LOG);
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.TOOL);
        assertEquals(63, broken.size());
    }

    @Test
    void aSmallTreeIsFelledWholeAndItsCanopyJoins() {
        trunk(5, Material.BIRCH_LOG);
        world.getBlockAt(0, 15, 0).setType(Material.BIRCH_LEAVES);
        world.getBlockAt(1, 14, 0).setType(Material.BIRCH_LEAVES);
        world.getBlockAt(5, 14, 0).setType(Material.BIRCH_LEAVES); // far from the logs
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.TOOL);
        assertEquals(6, broken.size()); // 4 logs + 2 leaves
        assertFalse(broken.contains(world.getBlockAt(5, 14, 0)));
    }

    @Test
    void logsComeFirstSoTheCanopyFillsTheRestOfTheCap() {
        trunk(10, Material.OAK_LOG);
        for (int y = 10; y < 20; y++) {
            world.getBlockAt(1, y, 0).setType(Material.OAK_LEAVES);
        }
        chop(world.getBlockAt(0, 10, 0), axe(1), BreakCause.TOOL); // cap 16: 9 logs + 6 leaves
        assertEquals(15, broken.size());
        assertEquals(9, broken.stream().filter(b -> b.getX() == 0).count()); // every log, then 6 leaves
    }

    @Test
    void onlyLogsAtOrAboveTheBrokenBlockAreCut() {
        trunk(6, Material.OAK_LOG);
        chop(world.getBlockAt(0, 13, 0), axe(3), BreakCause.TOOL);
        assertEquals(2, broken.size()); // y 14 and 15; the stump below stays
        assertTrue(broken.stream().allMatch(b -> b.getY() > 13));
    }

    @Test
    void otherWoodTypesAndPlacedBlocksAreLeftAlone() {
        trunk(6, Material.OAK_LOG);
        world.getBlockAt(0, 12, 0).setType(Material.SPRUCE_LOG);
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.TOOL);
        assertEquals(1, broken.size()); // only y 11: the spruce log cuts the trunk
        broken.clear();

        world.getBlockAt(0, 12, 0).setType(Material.OAK_LOG);
        tracker.add(world.getBlockAt(0, 12, 0), BlockTag.PLACED);
        world.getBlockAt(0, 11, 0).setType(Material.OAK_LOG);
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.TOOL);
        assertEquals(1, broken.size()); // the placed log cuts the trunk
    }

    @Test
    void netherStemsWithWartBlocksAndShroomlightAreFelled() {
        for (int y = 10; y < 14; y++) {
            world.getBlockAt(0, y, 0).setType(Material.CRIMSON_STEM);
        }
        world.getBlockAt(1, 13, 0).setType(Material.NETHER_WART_BLOCK);
        world.getBlockAt(-1, 13, 0).setType(Material.SHROOMLIGHT);
        world.getBlockAt(0, 14, 0).setType(Material.WARPED_WART_BLOCK);
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.TOOL);
        assertEquals(6, broken.size()); // 3 stems + 3 canopy blocks
    }

    @Test
    void aHatWiderThanTheStemIsFelledThroughCanopyContactUpToTheReach() {
        for (int y = 10; y < 14; y++) {
            world.getBlockAt(0, y, 0).setType(Material.CRIMSON_STEM);
        }
        for (int x = 1; x <= 6; x++) { // a row of wart blocks leaving the top stem sideways
            world.getBlockAt(x, 13, 0).setType(Material.NETHER_WART_BLOCK);
        }
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.TOOL);
        assertEquals(3 + TreeScan.CANOPY_REACH, broken.size()); // blocks 5 and 6 are too far from a log
        assertFalse(broken.contains(world.getBlockAt(5, 13, 0)));
    }

    @Test
    void onlyAToolBreakStartsAFelling() {
        trunk(5, Material.OAK_LOG);
        chop(world.getBlockAt(0, 10, 0), axe(3), BreakCause.EFFECT);
        assertEquals(0, broken.size());
    }

    @Test
    void aNonLogBlockOrAToolWithoutTheRuneDoesNothing() {
        trunk(5, Material.OAK_LOG);
        world.getBlockAt(3, 10, 0).setType(Material.DIRT);
        chop(world.getBlockAt(3, 10, 0), axe(3), BreakCause.TOOL);
        ItemStack plain = items.create(
                items.register(RuneItem.tool(
                        "fell_plain", Rarity.EPIC, ToolKind.AXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        chop(world.getBlockAt(0, 10, 0), plain, BreakCause.TOOL);
        assertEquals(0, broken.size());
    }
}
