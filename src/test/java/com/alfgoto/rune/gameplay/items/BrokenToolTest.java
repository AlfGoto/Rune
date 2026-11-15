package com.alfgoto.rune.gameplay.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.text.Rarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class BrokenToolTest {

    private ServerMock server;
    private ItemRegistry registry;
    private BrokenToolListener listener;
    private ItemStack pick;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        registry = new ItemRegistry(plugin);
        listener = new BrokenToolListener(registry);
        RuneItem item = registry.register(
                RuneItem.tool("broken_test_pick", Rarity.COMMON, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        pick = registry.create(item, 1);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void setDamage(int damage) {
        Damageable meta = (Damageable) pick.getItemMeta();
        meta.setDamage(damage);
        pick.setItemMeta(meta);
    }

    private int maxDamage() {
        return ((Damageable) pick.getItemMeta()).getMaxDamage();
    }

    @Test
    void lastHitLeavesTheToolAtZeroDurability() {
        Player player = server.addPlayer();
        setDamage(maxDamage() - 1);
        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, pick, 1);
        listener.onItemDamage(event);
        assertTrue(event.isCancelled());
        assertEquals(maxDamage(), ((Damageable) pick.getItemMeta()).getDamage());
        assertTrue(listener.isBroken(pick));
        assertTrue(registry.of(pick).isPresent());
    }

    @Test
    void healthyToolIsDamagedNormally() {
        Player player = server.addPlayer();
        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, pick, 1);
        listener.onItemDamage(event);
        assertFalse(event.isCancelled());
        assertFalse(listener.isBroken(pick));
    }

    @Test
    void brokenToolCannotMine() {
        Player player = server.addPlayer();
        setDamage(maxDamage());
        player.getInventory().setItemInMainHand(pick);
        BlockBreakEvent event = new BlockBreakEvent(player.getWorld().getBlockAt(0, 64, 0), player);
        listener.onBlockBreak(event);
        assertTrue(event.isCancelled());
    }

    @Test
    void vanillaToolIsNeverBroken() {
        assertFalse(listener.isBroken(new ItemStack(Material.WOODEN_PICKAXE)));
    }
}
