package com.alfgoto.rune.content.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.gameplay.veins.TunnelScan;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** The Obsidian and Tunneler Pickaxes: stats, Traits, recipes and the 3x3 square. */
class TunnelerTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private ToolLoadout loadout;
    private final List<Block> broken = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("tunnel");
        items = plugin.context().get(ItemRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        server.getScheduler().performOneTick(); // Rune recipes reach the server one tick later
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private TunnelerListener listener() {
        MultiBreak multi = new MultiBreak(plugin.context().get(Triggers.class), (p, block) -> {
            broken.add(block);
            block.setType(Material.AIR);
            return true;
        });
        return new TunnelerListener(items, loadout, multi, stack -> false);
    }

    private void mine(String toolId, BreakCause cause) {
        ItemStack tool = items.create(items.get(toolId), 1);
        player.getInventory().setItemInMainHand(tool);
        for (int x = -2; x <= 2; x++) {
            for (int y = 8; y <= 12; y++) {
                for (int z = -2; z <= 2; z++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
            }
        }
        listener().onBlockBreak(new RuneBlockBreakEvent(player, world.getBlockAt(0, 10, 0), cause, tool));
    }

    @Test
    void bothAreEpicWithTheirCapacity() {
        assertEquals(Rarity.EPIC, items.get("obsidian_pickaxe").rarity());
        assertEquals(Rarity.EPIC, items.get("tunneler").rarity());
        assertEquals(17, loadout.capacity(items.create(items.get("obsidian_pickaxe"), 1)));
        assertEquals(11, loadout.capacity(items.create(items.get("tunneler"), 1)));
    }

    @Test
    void obsidianIsNearlyUnbreakableAndSlow() {
        var sources = loadout.sources(items.create(items.get("obsidian_pickaxe"), 1));
        assertEquals(95, Stacking.total("sturdy", sources));
        assertEquals(-20, Stacking.total("haste", sources));
    }

    @Test
    void tunnelerIsSlowerAndHasTheFlag() {
        var sources = loadout.sources(items.create(items.get("tunneler"), 1));
        assertEquals(1, Stacking.total("tunneler", sources));
        assertEquals(-20, Stacking.total("haste", sources));
    }

    @Test
    void recipesExist() {
        RecipeBook book = plugin.context().get(RecipeBook.class);
        assertEquals(18, book.find("obsidian_pickaxe").orElseThrow().level());
        assertEquals(20, book.find("tunneler").orElseThrow().level());
        assertNotNull(server.getRecipe(book.key("obsidian_pickaxe")));
        assertNotNull(server.getRecipe(book.key("tunneler")));
    }

    @Test
    void theTunnelerBreaksEightBlocksAroundTheMinedOne() {
        mine("tunneler", BreakCause.TOOL);
        assertEquals(8, broken.size());
        assertFalse(broken.contains(world.getBlockAt(0, 10, 0)));
    }

    @Test
    void otherPickaxesAndEffectBreaksMineOnlyOneBlock() {
        mine("obsidian_pickaxe", BreakCause.TOOL);
        mine("tunneler", BreakCause.EFFECT);
        assertEquals(0, broken.size());
    }

    @Test
    void squareFollowsTheFace() {
        Block origin = world.getBlockAt(0, 10, 0);
        for (BlockFace face : List.of(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST)) {
            List<Block> square = TunnelScan.square(origin, face, b -> true);
            assertEquals(8, square.size());
            for (Block block : square) {
                // The square is flat on the face's axis.
                assertEquals(
                        0,
                        block.getX() * face.getModX() + (block.getY() - 10) * face.getModY()
                                + block.getZ() * face.getModZ());
            }
        }
        assertTrue(TunnelScan.square(origin, BlockFace.SELF, b -> true).isEmpty());
    }

    @Test
    void squareSkipsRefusedBlocks() {
        Block origin = world.getBlockAt(0, 10, 0);
        assertEquals(0, TunnelScan.square(origin, BlockFace.UP, b -> false).size());
    }
}
