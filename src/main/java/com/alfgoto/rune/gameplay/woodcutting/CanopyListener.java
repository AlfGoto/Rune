package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Canopy Stride: while the player holds a Rune Tool with the Rune and stands on leaves or logs, their move
 * speed goes up. It is a temporary modifier on the movement speed, replaced whenever the player changes
 * block or item.
 */
final class CanopyListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final NamespacedKey key;

    CanopyListener(Plugin plugin, ItemRegistry items, ToolLoadout loadout) {
        this.items = items;
        this.loadout = loadout;
        this.key = new NamespacedKey(plugin, "canopy_stride");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }
        Player player = event.getPlayer();
        refresh(player, player.getInventory().getItemInMainHand(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        refresh(player, player.getInventory().getItem(event.getNewSlot()), player.getLocation());
    }

    private void refresh(Player player, ItemStack tool, Location at) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(key);
        if (tool == null || items.of(tool).isEmpty()) {
            return;
        }
        Block below = at.getBlock().getRelative(BlockFace.DOWN);
        if (!Tag.LEAVES.isTagged(below.getType()) && !Tag.LOGS.isTagged(below.getType())) {
            return;
        }
        double percent = loadout.total(tool, LeafRules.CANOPY_STRIDE);
        if (percent > 0) {
            speed.addTransientModifier(
                    new AttributeModifier(key, percent / 100, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }
}
