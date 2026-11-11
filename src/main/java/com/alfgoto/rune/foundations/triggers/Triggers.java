package com.alfgoto.rune.foundations.triggers;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Turns Paper events into Rune events.
 *
 * <p>{@link BlockBreakEvent} is only fired for players, so TNT and creepers never reach Rune events.
 */
public final class Triggers implements Listener {

    private final Server server;
    private boolean effectBreak;

    public Triggers(Server server) {
        this.server = server;
    }

    /**
     * Runs {@code action} (e.g. a Rune effect that breaks blocks for a player). Player breaks made inside are
     * tagged {@link BreakCause#EFFECT}.
     */
    public void asEffect(Runnable action) {
        boolean previous = effectBreak;
        effectBreak = true;
        try {
            action.run();
        } finally {
            effectBreak = previous;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        call(new RuneBlockBreakEvent(event.getPlayer(), event.getBlock(), causeOf(item), item));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ClickType click;
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            click = ClickType.LEFT;
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            click = ClickType.RIGHT;
        } else {
            return;
        }
        call(new RuneClickEvent(
                event.getPlayer(), click, event.getPlayer().getInventory().getItemInMainHand()));
    }

    private BreakCause causeOf(ItemStack item) {
        if (effectBreak) {
            return BreakCause.EFFECT;
        }
        return item.getType().isAir() ? BreakCause.HAND : BreakCause.TOOL;
    }

    private void call(Event event) {
        server.getPluginManager().callEvent(event);
    }
}
