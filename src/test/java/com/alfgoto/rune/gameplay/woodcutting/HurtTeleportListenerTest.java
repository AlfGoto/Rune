package com.alfgoto.rune.gameplay.woodcutting;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class HurtTeleportListenerTest {

    private ServerMock server;
    private RunePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        server.getScheduler().performOneTick();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void chorusAxeTeleportsWhenHurt() {
        World world = server.addSimpleWorld("world");
        for (int x = -20; x <= 20; x++) {
            for (int z = -20; z <= 20; z++) {
                world.getBlockAt(x, 63, z).setType(Material.STONE);
            }
        }
        PlayerMock player = server.addPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(new Location(world, 0.5, 64, 0.5));
        ItemRegistry items = plugin.context().get(ItemRegistry.class);
        player.getInventory().setItemInMainHand(items.create(items.get("chorus_axe"), 1));
        Location before = player.getLocation().clone();
        player.damage(2);
        server.getScheduler().performTicks(2);
        assertNotEquals(before.getBlockX() + "," + before.getBlockZ(),
                player.getLocation().getBlockX() + "," + player.getLocation().getBlockZ());
    }
}
