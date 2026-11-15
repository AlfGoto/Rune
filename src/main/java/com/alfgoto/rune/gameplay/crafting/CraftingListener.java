package com.alfgoto.rune.gameplay.crafting;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/** A locked recipe cannot be crafted: the result is removed and the craft is cancelled. */
public final class CraftingListener implements Listener {

    private final RecipeBook book;

    public CraftingListener(RecipeBook book) {
        this.book = book;
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        CraftingRecipe recipe = book.of(event.getRecipe()).orElse(null);
        if (recipe != null && !book.isUnlocked(event.getView().getPlayer().getUniqueId(), recipe)) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        CraftingRecipe recipe = book.of(event.getRecipe()).orElse(null);
        if (recipe != null && !book.isUnlocked(event.getWhoClicked().getUniqueId(), recipe)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        book.sync(event.getPlayer());
    }
}
