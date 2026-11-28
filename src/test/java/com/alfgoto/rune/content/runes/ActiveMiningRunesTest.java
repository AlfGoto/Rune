package com.alfgoto.rune.content.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.gameplay.blast.BlastArea;
import com.alfgoto.rune.gameplay.blast.BlastCharges;
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
import java.util.concurrent.atomic.AtomicLong;
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

/** Ore Dash, Blast Mine and Great Blast: the Runes, their cooldowns, the armed charge and the blast areas. */
class ActiveMiningRunesTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private BlastCharges charges;
    private final List<Block> broken = new ArrayList<>();
    private int picks;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("blast");
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        charges = new BlastCharges();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack pickaxe(String rune, int rank) {
        ItemStack pick = items.create(
                items.register(RuneItem.tool(
                        "blast_pick_" + picks++, Rarity.EPIC, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(pick, runes.get(rune), rank));
        return pick;
    }

    private BlastListener listener() {
        MultiBreak multi = new MultiBreak(plugin.context().get(Triggers.class), (p, block) -> {
            broken.add(block);
            block.setType(Material.AIR);
            return true;
        });
        return new BlastListener(items, loadout, charges, multi, stack -> false);
    }

    private void mine(Block block, ItemStack tool, BreakCause cause) {
        player.getInventory().setItemInMainHand(tool);
        listener().onBlockBreak(new RuneBlockBreakEvent(player, block, cause, tool));
    }

    private void fillStone() {
        for (int x = -3; x <= 3; x++) {
            for (int y = 8; y <= 14; y++) {
                for (int z = -3; z <= 3; z++) {
                    world.getBlockAt(x, y, z).setType(Material.STONE);
                }
            }
        }
    }

    @Test
    void theRunesAreActivePickaxeOnlyWithTheDocumentedRarityRangeAndCooldowns() {
        var dash = runes.get("ore_dash");
        assertEquals(Rarity.RARE, dash.item().rarity());
        assertEquals(Set.of(Fit.PICKAXE), dash.fits());
        assertTrue(dash.isActive());
        assertEquals(8, dash.effect().valueAt(1));
        assertEquals(12, dash.effect().valueAt(2));
        assertEquals(16, dash.effect().valueAt(3));
        assertEquals(20, dash.cooldownSeconds(1));
        assertEquals(20, dash.cooldownSeconds(3));

        var blast = runes.get("blast_mine");
        assertEquals(Rarity.EPIC, blast.item().rarity());
        assertEquals(Set.of(Fit.PICKAXE), blast.fits());
        assertEquals(30, blast.cooldownSeconds(1));
        assertEquals(10, blast.cooldownSeconds(3));

        var great = runes.get("great_blast");
        assertEquals(Rarity.LEGENDARY, great.item().rarity());
        assertEquals(60, great.cooldownSeconds(1));
        assertEquals(20, great.cooldownSeconds(3));
    }

    @Test
    void aChargeIsSpentOnceAndEndsByItself() {
        AtomicLong now = new AtomicLong(0);
        BlastCharges timed = new BlastCharges(now::get);
        var id = player.getUniqueId();
        assertFalse(timed.consume(id, "blast_mine"));
        timed.arm(id, "blast_mine", 20);
        assertTrue(timed.isArmed(id, "blast_mine"));
        assertTrue(timed.consume(id, "blast_mine"));
        assertFalse(timed.consume(id, "blast_mine"));
        timed.arm(id, "blast_mine", 20);
        now.set(20_000);
        assertFalse(timed.isArmed(id, "blast_mine"));
    }

    @Test
    void blastMineBreaksTheEightBlocksAroundInTheFlatPlane() {
        fillStone();
        charges.arm(player.getUniqueId(), "blast_mine", 20);
        var origin = world.getBlockAt(0, 10, 0);
        // The square is the one the listener builds: here the plane is flat to Y.
        var square = BlastArea.square(origin, BlastArea.Axis.Y, b -> true);
        assertEquals(8, square.size());
        assertTrue(square.stream().allMatch(b -> b.getY() == 10));
        var wall = BlastArea.square(origin, BlastArea.Axis.Z, b -> true);
        assertTrue(wall.stream().allMatch(b -> b.getZ() == 0));
        assertEquals(8, wall.size());
    }

    @Test
    void greatBlastBreaksTheTwentySixBlocksAroundAndTheChargeIsSpent() {
        fillStone();
        charges.arm(player.getUniqueId(), "great_blast", 20);
        mine(world.getBlockAt(0, 10, 0), pickaxe("great_blast", 1), BreakCause.TOOL);
        assertEquals(26, broken.size());
        assertFalse(charges.isArmed(player.getUniqueId(), "great_blast"));
        broken.clear();
        mine(world.getBlockAt(0, 12, 0), pickaxe("great_blast", 1), BreakCause.TOOL);
        assertEquals(0, broken.size()); // no charge left
    }

    @Test
    void blastMineBreaksAFlatSquareOfEightAroundTheMinedBlock() {
        fillStone();
        charges.arm(player.getUniqueId(), "blast_mine", 20);
        mine(world.getBlockAt(0, 10, 0), pickaxe("blast_mine", 1), BreakCause.TOOL);
        assertEquals(8, broken.size());
        assertTrue(broken.stream().allMatch(b -> b.getY() == 10));
    }

    @Test
    void nothingHappensWithoutACharge_anEffectBreak_orABlockThatIsNotAPickaxeBlock() {
        fillStone();
        var tool = pickaxe("great_blast", 1);
        mine(world.getBlockAt(0, 10, 0), tool, BreakCause.TOOL);
        assertEquals(0, broken.size()); // not armed

        charges.arm(player.getUniqueId(), "great_blast", 20);
        mine(world.getBlockAt(0, 10, 0), tool, BreakCause.EFFECT);
        assertEquals(0, broken.size()); // effects never chain

        world.getBlockAt(0, 10, 0).setType(Material.DIRT);
        mine(world.getBlockAt(0, 10, 0), tool, BreakCause.TOOL);
        assertEquals(0, broken.size()); // dirt is a shovel block: the charge is kept
        assertTrue(charges.isArmed(player.getUniqueId(), "great_blast"));
    }

    @Test
    void onlyBreakablePickaxeBlocksAreTaken() {
        fillStone();
        world.getBlockAt(1, 10, 0).setType(Material.BEDROCK);
        world.getBlockAt(-1, 10, 0).setType(Material.DIRT);
        world.getBlockAt(0, 11, 0).setType(Material.AIR);
        charges.arm(player.getUniqueId(), "great_blast", 20);
        mine(world.getBlockAt(0, 10, 0), pickaxe("great_blast", 1), BreakCause.TOOL);
        assertEquals(23, broken.size());
        assertFalse(broken.contains(world.getBlockAt(1, 10, 0)));
        assertFalse(broken.contains(world.getBlockAt(-1, 10, 0)));
    }

    @Test
    void aToolWithoutTheRuneDoesNotSetTheChargeOff() {
        fillStone();
        charges.arm(player.getUniqueId(), "great_blast", 20);
        mine(world.getBlockAt(0, 10, 0), pickaxe("blast_mine", 1), BreakCause.TOOL);
        assertEquals(0, broken.size());
        assertTrue(charges.isArmed(player.getUniqueId(), "great_blast"));
    }
}
