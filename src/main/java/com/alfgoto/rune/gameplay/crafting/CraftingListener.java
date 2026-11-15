package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * A locked recipe cannot be crafted: the result is removed and the craft is cancelled.
 * An unlocked Rune recipe gets a fresh result, so it goes through the redrawers (name, lore) like /rune give.
 */
public final class CraftingListener implements Listener {

    private final RecipeBook book;
    private final ItemRegistry items;

    public CraftingListener(RecipeBook book, ItemRegistry items) {
        this.book = book;
        this.items = items;
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        CraftingRecipe recipe = book.of(event.getRecipe()).orElse(null);
        if (recipe == null) {
            return;
        }
        if (book.isUnlocked(event.getView().getPlayer().getUniqueId(), recipe)) {
            event.getInventory().setResult(items.create(items.get(recipe.result()), recipe.amount()));
        } else {
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
