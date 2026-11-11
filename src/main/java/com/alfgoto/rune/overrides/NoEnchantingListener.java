package com.alfgoto.rune.overrides;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

/** Removes vanilla enchanting: enchanting table, enchanted books, anvil enchant combining. Repairs stay. */
public class NoEnchantingListener implements Listener {

    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result == null) {
            return;
        }
        AnvilInventory anvil = event.getInventory();
        ItemStack first = anvil.getFirstItem();
        ItemStack second = anvil.getSecondItem();
        if (isBook(first) || isBook(second) || isBook(result) || changesEnchantments(first, result)) {
            event.setResult(null);
        }
    }

    private static boolean isBook(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_BOOK;
    }

    /** A repair or rename keeps the enchantments of the first item; anything else is combining. */
    private static boolean changesEnchantments(ItemStack first, ItemStack result) {
        if (first == null) {
            return !result.getEnchantments().isEmpty();
        }
        return !first.getEnchantments().equals(result.getEnchantments());
    }
}
