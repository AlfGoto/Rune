package com.alfgoto.rune.overrides;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Locked Gear can't be crafted, used (mine, attack, right-click) or worn. */
public class LockedGearListener implements Listener {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (LockedGear.isLocked(event.getInventory().getResult())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (LockedGear.isLocked(event.getRecipe().getResult())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (LockedGear.isLocked(event.getResult())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (LockedGear.isLocked(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (LockedGear.isLocked(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player
                && LockedGear.isLocked(player.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    /** Stops right-click uses: tilling, stripping, path making, equipping armor from the hand. */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (LockedGear.isLocked(event.getItem())) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onDispenseArmor(BlockDispenseArmorEvent event) {
        if (LockedGear.isLocked(event.getItem())) {
            event.setCancelled(true);
        }
    }

    /** Covers every way to equip: clicks, shift-click, hotkeys, drag, right-click. */
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (LockedGear.isLocked(event.getNewItem())) {
            unequipAll(event.getPlayer());
        }
    }

    /** Found or old armor already worn on join comes off. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        unequipAll(event.getPlayer());
    }

    private static void unequipAll(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack worn = inventory.getItem(slot);
            if (!LockedGear.isLocked(worn)) {
                continue;
            }
            inventory.setItem(slot, null);
            Map<Integer, ItemStack> leftover = inventory.addItem(worn);
            leftover.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
        }
    }
}
