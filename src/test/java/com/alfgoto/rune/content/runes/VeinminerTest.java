package com.alfgoto.rune.content.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
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

/** Veinminer: the cap per Rank, which blocks join the vein, and that the extra breaks are Effect breaks. */
class VeinminerTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private BlockTracker tracker;
    private final List<Block> broken = new ArrayList<>();
    private int picks;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("vein");
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        tracker = plugin.context().get(BlockTracker.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack pick(int rank) {
        ItemStack pick = items.create(
                items.register(RuneItem.tool(
                        "vein_pick_" + picks++, Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(pick, runes.get("veinminer"), rank));
        return pick;
    }

    private VeinminerListener listener() {
        MultiBreak multi = new MultiBreak(plugin.context().get(Triggers.class), (p, block) -> {
            broken.add(block);
            block.setType(Material.AIR);
            return true;
        });
        return new VeinminerListener(items, loadout, tracker, multi, stack -> false);
    }

    private void mine(Block block, ItemStack tool, BreakCause cause) {
        player.getInventory().setItemInMainHand(tool);
        listener().onBlockBreak(new RuneBlockBreakEvent(player, block, cause, tool));
    }

    /** A line of {@code length} iron ore blocks along x at y 10. */
    private void line(int length) {
        for (int x = 0; x < length; x++) {
            world.getBlockAt(x, 10, 0).setType(Material.IRON_ORE);
        }
    }

    @Test
    void theRuneIsLegendaryPickaxeOnlyAndCapsAre8_16_32() {
        var rune = runes.get("veinminer");
        assertEquals(Rarity.LEGENDARY, rune.item().rarity());
        assertEquals(java.util.Set.of(com.alfgoto.rune.gameplay.runes.Fit.PICKAXE), rune.fits());
        assertEquals(8, rune.effect().valueAt(1));
        assertEquals(16, rune.effect().valueAt(2));
        assertEquals(32, rune.effect().valueAt(3));
        assertEquals(3, rune.maxRank().getAsInt());
    }

    @Test
    void theCapCountsTheFirstBlock() {
        line(50);
        mine(world.getBlockAt(0, 10, 0), pick(1), BreakCause.TOOL);
        assertEquals(7, broken.size()); // 8 blocks in total with the one the player broke
        broken.clear();
        line(50);
        mine(world.getBlockAt(0, 10, 0), pick(3), BreakCause.TOOL);
        assertEquals(31, broken.size());
    }

    @Test
    void aSmallVeinIsMinedWhole() {
        line(3);
        mine(world.getBlockAt(0, 10, 0), pick(3), BreakCause.TOOL);
        assertEquals(2, broken.size());
    }

    @Test
    void deepslateAndPlainOreOfTheSameKindJoinButOtherOresDoNot() {
        world.getBlockAt(0, 10, 0).setType(Material.IRON_ORE);
        world.getBlockAt(1, 10, 0).setType(Material.DEEPSLATE_IRON_ORE);
        world.getBlockAt(2, 10, 0).setType(Material.GOLD_ORE);
        world.getBlockAt(1, 11, 1).setType(Material.IRON_ORE); // touches by a corner... of (0,10,0)
        mine(world.getBlockAt(0, 10, 0), pick(3), BreakCause.TOOL);
        assertEquals(2, broken.size());
        assertTrue(broken.stream().noneMatch(b -> b.getType() == Material.GOLD_ORE));
    }

    @Test
    void placedOresAreLeftAloneAndStoneIsNotAVein() {
        world.getBlockAt(0, 10, 0).setType(Material.IRON_ORE);
        world.getBlockAt(1, 10, 0).setType(Material.IRON_ORE);
        world.getBlockAt(2, 10, 0).setType(Material.IRON_ORE);
        world.getBlockAt(0, 11, 0).setType(Material.STONE);
        tracker.add(world.getBlockAt(1, 10, 0), BlockTag.PLACED);
        mine(world.getBlockAt(0, 10, 0), pick(3), BreakCause.TOOL);
        assertEquals(0, broken.size()); // the placed ore cuts the vein; (2,10,0) is not connected
    }

    @Test
    void onlyAToolBreakStartsAVein() {
        line(5);
        mine(world.getBlockAt(0, 10, 0), pick(3), BreakCause.EFFECT);
        assertEquals(0, broken.size());
    }

    @Test
    void aStoneBlockOrAToolWithoutTheRuneDoesNothing() {
        line(5);
        world.getBlockAt(0, 11, 0).setType(Material.STONE);
        mine(world.getBlockAt(0, 11, 0), pick(3), BreakCause.TOOL);
        ItemStack plain = items.create(
                items.register(RuneItem.tool(
                        "vein_plain", Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        mine(world.getBlockAt(0, 10, 0), plain, BreakCause.TOOL);
        assertEquals(0, broken.size());
    }
}
