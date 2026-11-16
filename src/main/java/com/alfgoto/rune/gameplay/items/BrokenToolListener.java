package com.alfgoto.rune.gameplay.items;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
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

    private boolean isRuneGear(ItemStack stack) {
        return items.of(stack).filter(item -> item.toolSpec().isPresent() || item.armorSpec().isPresent()).isPresent();
    }

    /** Tools only: a Broken armor piece held in the hand must not block mining or attacking. */
    private boolean isBrokenTool(ItemStack stack) {
        return stack != null && items.of(stack).flatMap(RuneItem::toolSpec).isPresent() && isBroken(stack);
    }

    private boolean isRuneArmor(ItemStack stack) {
        return items.of(stack).flatMap(RuneItem::armorSpec).isPresent();
    }

    /** True when {@code stack} is a Rune Tool or Rune Armor piece with no durability left. */
    public boolean isBroken(ItemStack stack) {
        if (stack == null || !isRuneGear(stack)) {
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
        if (!isRuneGear(stack) || !(stack.getItemMeta() instanceof Damageable meta) || !meta.hasMaxDamage()) {
            return;
        }
        if (meta.getDamage() + event.getDamage() < meta.getMaxDamage()) {
            return;
        }
        event.setCancelled(true);
        meta.setDamage(meta.getMaxDamage());
        stack.setItemMeta(meta);
        if (isRuneArmor(stack)) {
            // A Broken piece gives no protection: drop its attribute modifiers until it is repaired.
            ArmorComponents.clear(stack);
        }
    }

    /** Anvil repair: a repaired armor piece gets its protection back. */
    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && isRuneArmor(result) && !isBroken(result)) {
            restoreProtection(result);
            event.setResult(result);
        }
    }

    private void restoreProtection(ItemStack stack) {
        items.of(stack).flatMap(items::armorStats).ifPresent(stats -> ArmorComponents.apply(stack, stats));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (isBrokenTool(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isBrokenTool(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && isBrokenTool(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (isBrokenTool(event.getItem())) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }
}
