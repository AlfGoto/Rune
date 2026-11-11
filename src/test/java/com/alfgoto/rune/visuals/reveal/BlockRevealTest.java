package com.alfgoto.rune.visuals.reveal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alfgoto.rune.RunePlugin;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class BlockRevealTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private Block block;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        block = player.getWorld().getBlockAt(0, 64, 0);
        block.setType(Material.DIAMOND_ORE);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private long displays() {
        return player.getWorld().getEntitiesByClass(BlockDisplay.class).size();
    }

    @Test
    void outlinesAreRemovedWhenTimeIsUp() {
        plugin.blockReveal().reveal(player, List.of(block), 40, Color.AQUA);
        assertEquals(1, plugin.blockReveal().count(player.getUniqueId()));
        assertEquals(1, displays());

        server.getScheduler().performTicks(40);

        assertEquals(0, plugin.blockReveal().count(player.getUniqueId()));
        assertEquals(0, displays());
    }

    @Test
    void outlinesAreRemovedWhenPlayerLeaves() {
        plugin.blockReveal().reveal(player, List.of(block), 400, Color.AQUA);
        player.disconnect();
        assertEquals(0, displays());
    }

    @Test
    void outlinesAreRemovedOnDisable() {
        plugin.blockReveal().reveal(player, List.of(block), 400, Color.AQUA);
        plugin.blockReveal().clearAll();
        assertEquals(0, displays());
    }
}
