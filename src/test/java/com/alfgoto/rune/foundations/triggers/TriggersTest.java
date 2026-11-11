package com.alfgoto.rune.foundations.triggers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class TriggersTest {

    private ServerMock server;
    private Triggers triggers;
    private PlayerMock player;
    private final List<RuneBlockBreakEvent> breaks = new ArrayList<>();
    private final List<RuneClickEvent> clicks = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Plugin plugin = MockBukkit.createMockPlugin();
        triggers = new Triggers(server);
        player = server.addPlayer();
        server.getPluginManager().registerEvents(triggers, plugin);
        server.getPluginManager().registerEvents(new Collector(), plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Block stone() {
        Block block = player.getWorld().getBlockAt(0, 64, 0);
        block.setType(Material.STONE);
        return block;
    }

    @Test
    void playerBreakWithToolIsTool() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.WOODEN_PICKAXE));
        server.getPluginManager().callEvent(new BlockBreakEvent(stone(), player));
        assertEquals(1, breaks.size());
        assertEquals(BreakCause.TOOL, breaks.get(0).cause());
    }

    @Test
    void playerBreakWithEmptyHandIsHand() {
        server.getPluginManager().callEvent(new BlockBreakEvent(stone(), player));
        assertEquals(BreakCause.HAND, breaks.get(0).cause());
    }

    @Test
    void breakInsideEffectIsEffect() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.WOODEN_PICKAXE));
        triggers.asEffect(() -> server.getPluginManager().callEvent(new BlockBreakEvent(stone(), player)));
        server.getPluginManager().callEvent(new BlockBreakEvent(stone(), player));
        assertEquals(BreakCause.EFFECT, breaks.get(0).cause());
        assertEquals(BreakCause.TOOL, breaks.get(1).cause());
    }

    @Test
    void cancelledBreakIsIgnored() {
        BlockBreakEvent event = new BlockBreakEvent(stone(), player);
        event.setCancelled(true);
        server.getPluginManager().callEvent(event);
        assertTrue(breaks.isEmpty());
    }

    @Test
    void leftAndRightClickInAirAreSeparate() {
        player.getInventory().setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
        interact(Action.LEFT_CLICK_AIR, EquipmentSlot.HAND);
        interact(Action.RIGHT_CLICK_AIR, EquipmentSlot.HAND);
        assertEquals(
                List.of(ClickType.LEFT, ClickType.RIGHT),
                clicks.stream().map(RuneClickEvent::click).toList());
        assertEquals(Material.WOODEN_SWORD, clicks.get(0).item().getType());
    }

    @Test
    void blockClicksAndOffHandAreIgnored() {
        interact(Action.LEFT_CLICK_BLOCK, EquipmentSlot.HAND);
        interact(Action.RIGHT_CLICK_BLOCK, EquipmentSlot.HAND);
        interact(Action.RIGHT_CLICK_AIR, EquipmentSlot.OFF_HAND);
        assertTrue(clicks.isEmpty());
    }

    private void interact(Action action, EquipmentSlot hand) {
        server.getPluginManager()
                .callEvent(new PlayerInteractEvent(
                        player, action, player.getInventory().getItemInMainHand(), null, BlockFace.SELF, hand));
    }

    private final class Collector implements Listener {
        @EventHandler
        void onBreak(RuneBlockBreakEvent event) {
            breaks.add(event);
        }

        @EventHandler
        void onClick(RuneClickEvent event) {
            clicks.add(event);
        }
    }
}
