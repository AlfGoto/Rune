package com.alfgoto.rune.gameplay.caches;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Right-click (in the air or on a block) with a Rune Cache in the main hand opens it. */
final class CacheListener implements Listener {

    private final Caches caches;

    CacheListener(Caches caches) {
        this.caches = caches;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (caches.rarityOf(event.getItem()).isEmpty()) {
            return;
        }
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        PlayerInventory inventory = event.getPlayer().getInventory();
        ItemStack hand = inventory.getItemInMainHand();
        if (caches.open(event.getPlayer(), hand)) {
            inventory.setItemInMainHand(hand.getAmount() <= 0 ? null : hand);
        }
    }
}
