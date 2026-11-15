package com.alfgoto.rune.gameplay.items;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

/**
 * Broken state: a Rune Tool at 0 durability is unusable but never disappears and keeps its data.
 * The vanilla break is stopped (the tool stays at 0 durability), then every use is cancelled.
 * Repair is vanilla: the anvil restores durability for vanilla XP.
 */
public final class BrokenToolListener implements Listener {

    private final ItemRegistry items;

    public BrokenToolListener(ItemRegistry items) {
        this.items = items;
    }

    private boolean isRuneTool(ItemStack stack) {
        return items.of(stack).flatMap(RuneItem::toolSpec).isPresent();
    }

    /** True when {@code stack} is a Rune Tool with no durability left. */
    public boolean isBroken(ItemStack stack) {
        if (stack == null || !isRuneTool(stack)) {
            return false;
        }
        if (!(stack.getItemMeta() instanceof Damageable meta) || !meta.hasMaxDamage()) {
            return false;
        }
        return meta.getDamage() >= meta.getMaxDamage();
    }

    /** The last hit leaves the tool at 0 durability instead of destroying it. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack stack = event.getItem();
        if (!isRuneTool(stack) || !(stack.getItemMeta() instanceof Damageable meta) || !meta.hasMaxDamage()) {
            return;
        }
        if (meta.getDamage() + event.getDamage() < meta.getMaxDamage()) {
            return;
        }
        event.setCancelled(true);
        meta.setDamage(meta.getMaxDamage());
        stack.setItemMeta(meta);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (isBroken(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isBroken(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isBroken(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (isBroken(event.getItem())) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }
}
