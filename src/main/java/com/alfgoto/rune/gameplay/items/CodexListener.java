package com.alfgoto.rune.gameplay.items;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.PlayerInventory;

/** Keeps the {@link Codex} soulbound: given on join and respawn, never moved, dropped or lost on death. */
final class CodexListener implements Listener {

    private final Codex codex;

    CodexListener(Codex codex) {
        this.codex = codex;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        codex.ensure(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        codex.ensure(event.getPlayer());
    }

    /** The Codex never drops on death: it stays in the inventory, or comes back on respawn. */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(codex::is);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        if (codex.is(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    /** Any click on the Codex slot or on a Codex is cancelled: move, swap, hotkey, drop, shift-click. */
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        boolean codexSlot = event.getClickedInventory() instanceof PlayerInventory && event.getSlot() == Codex.SLOT;
        if (codexSlot || codex.is(event.getCurrentItem()) || codex.is(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        boolean touchesCodex = event.getRawSlots().stream()
                .anyMatch(raw -> event.getView().getInventory(raw) instanceof PlayerInventory
                        && event.getView().convertSlot(raw) == Codex.SLOT);
        if (touchesCodex || codex.is(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }
}
