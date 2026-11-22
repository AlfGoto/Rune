package com.alfgoto.rune.visuals.reveal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.text.Rarity;
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

class OreSenseTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private World world;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private BlockReveal reveal;
    private int picks;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        // The player joins before the plugin loads: MockBukkit has no getVirtualHost for the pack join listener.
        player = server.addPlayer();
        plugin = MockBukkit.load(RunePlugin.class);
        world = server.addSimpleWorld("sense");
        items = plugin.context().get(ItemRegistry.class);
        runes = plugin.context().get(RuneRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        reveal = plugin.context().get(BlockReveal.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private ItemStack pick(boolean withRune) {
        ItemStack pick = items.create(
                items.register(RuneItem.tool(
                        "sense_pick" + picks++, Rarity.LEGENDARY, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT)),
                1);
        if (withRune) {
            loadout.install(pick, runes.get("ore_sense"), 1);
        }
        return pick;
    }

    private OreSenseListener listener(double random) {
        return new OreSenseListener(items, loadout, reveal, () -> random, 8, 5);
    }

    private void breakBlock(OreSenseListener listener, Block block, ItemStack tool) {
        listener.onBlockBreak(new RuneBlockBreakEvent(player, block, BreakCause.TOOL, tool));
    }

    @Test
    void breakingStoneOutlinesOresWithinTheRadiusForFiveSeconds() {
        Block stone = world.getBlockAt(0, 10, 0);
        stone.setType(Material.STONE);
        world.getBlockAt(8, 10, 0).setType(Material.DIAMOND_ORE); // inside
        world.getBlockAt(0, 2, 0).setType(Material.DEEPSLATE_GOLD_ORE); // inside
        world.getBlockAt(9, 10, 0).setType(Material.IRON_ORE); // outside

        breakBlock(listener(0), stone, pick(true));
        assertEquals(2, reveal.count(player.getUniqueId()));

        server.getScheduler().performTicks(100); // 5 s
        assertEquals(0, reveal.count(player.getUniqueId()));
    }

    @Test
    void noOutlineOnFailedRollOnNonStoneOrWithoutTheRune() {
        world.getBlockAt(3, 10, 0).setType(Material.DIAMOND_ORE);
        Block stone = world.getBlockAt(0, 10, 0);
        stone.setType(Material.STONE);
        breakBlock(listener(0.99), stone, pick(true)); // 5% chance, roll 99
        assertEquals(0, reveal.count(player.getUniqueId()));

        breakBlock(listener(0), stone, pick(false));
        assertEquals(0, reveal.count(player.getUniqueId()));

        Block dirt = world.getBlockAt(0, 11, 0);
        dirt.setType(Material.DIRT);
        breakBlock(listener(0), dirt, pick(true));
        assertEquals(0, reveal.count(player.getUniqueId()));
    }
}
