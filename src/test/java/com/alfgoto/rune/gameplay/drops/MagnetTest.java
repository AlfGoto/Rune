package com.alfgoto.rune.gameplay.drops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class MagnetTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("magnet");
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void hold(int rank) {
        ItemStack pick = items.create(
                items.register(RuneItem.tool(
                        "magnet_pick_" + rank, Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        if (rank > 0) {
            assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(pick, runes.get("magnet"), rank));
        }
        player.getInventory().setItemInMainHand(pick);
    }

    private int count(Material material) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private BlockDropItemEvent drops(Block block, List<Item> dropped) {
        BlockDropItemEvent event = new BlockDropItemEvent(block, block.getState(), player, new ArrayList<>(dropped));
        server.getPluginManager().callEvent(event);
        return event;
    }

    @Test
    void theRuneHasTheDocsValues() {
        var magnet = runes.get("magnet");
        assertEquals(Rarity.UNCOMMON, magnet.item().rarity());
        assertEquals(2, magnet.effect().valueAt(1)); // radius +2 per Rank
        assertEquals(6, magnet.effect().valueAt(3));
    }

    @Test
    void dropsGoToTheInventoryWithMagnet() {
        hold(1);
        Block block = world.getBlockAt(0, 10, 0);
        Item dropped = world.dropItem(block.getLocation(), new ItemStack(Material.COBBLESTONE, 3));
        drops(block, List.of(dropped));
        assertEquals(3, count(Material.COBBLESTONE));
    }

    @Test
    void withoutMagnetNothingIsPulled() {
        hold(0);
        Block block = world.getBlockAt(0, 10, 0);
        Item dropped = world.dropItem(block.getLocation(), new ItemStack(Material.COBBLESTONE, 3));
        drops(block, List.of(dropped));
        assertEquals(0, count(Material.COBBLESTONE));
    }

    @Test
    void itemsInTheRadiusArePulledToo() {
        hold(1); // radius 2
        Block block = world.getBlockAt(0, 10, 0);
        Item near = world.dropItem(new Location(world, 1.5, 10.5, 0.5), new ItemStack(Material.DIAMOND, 2));
        Item far = world.dropItem(new Location(world, 8.5, 10.5, 0.5), new ItemStack(Material.EMERALD, 1));
        drops(block, List.of());
        assertEquals(2, count(Material.DIAMOND));
        assertTrue(near.isDead() || !near.isValid());
        assertEquals(0, count(Material.EMERALD)); // outside the radius
        assertFalse(far.isDead());
    }

    @Test
    void aBiggerRankReachesFurther() {
        hold(3); // radius 6
        Block block = world.getBlockAt(0, 10, 0);
        world.dropItem(new Location(world, 5.5, 10.5, 0.5), new ItemStack(Material.EMERALD, 1));
        drops(block, List.of());
        assertEquals(1, count(Material.EMERALD));
    }
}
