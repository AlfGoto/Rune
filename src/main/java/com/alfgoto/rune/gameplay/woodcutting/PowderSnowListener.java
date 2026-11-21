package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Spruce Axe: while it is in the main hand, the player does not freeze in powder snow and is not slowed
 * down by it. Freezing is cleared (no frost, no freeze damage). The slowdown is cancelled by a temporary
 * move speed bonus that is on only while the player stands in powder snow.
 */
final class PowderSnowListener implements Listener {

    private final Plugin plugin;
    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final NamespacedKey key;

    PowderSnowListener(Plugin plugin, ItemRegistry items, ToolLoadout loadout) {
        this.plugin = plugin;
        this.items = items;
        this.loadout = loadout;
        this.key = new NamespacedKey(plugin, "powder_snow_immunity");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (player.getFreezeTicks() > 0 && immune(player, tool)) {
            player.setFreezeTicks(0);
        }
        if (event.hasChangedBlock()) {
            refresh(player, tool, event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        refresh(player, player.getInventory().getItem(event.getNewSlot()), player.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFreezeDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FREEZE
                && event.getEntity() instanceof Player player
                && immune(player, player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            player.setFreezeTicks(0);
        }
    }

    // The axe can leave the main hand without a held-slot change. Refresh one tick later, once the item moved.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        later(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        later(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            later(player);
        }
    }

    private void later(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                refresh(player, player.getInventory().getItemInMainHand(), player.getLocation());
            }
        });
    }

    private boolean immune(Player player, ItemStack tool) {
        return tool != null
                && items.of(tool).isPresent()
                && loadout.total(tool, AxeTraits.POWDER_SNOW_IMMUNITY) > 0;
    }

    private void refresh(Player player, ItemStack tool, Location at) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(key);
        if (at.getBlock().getType() == Material.POWDER_SNOW && immune(player, tool)) {
            speed.addTransientModifier(new AttributeModifier(
                    key, AxeTraits.powderSnowCompensation(), AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }
}
