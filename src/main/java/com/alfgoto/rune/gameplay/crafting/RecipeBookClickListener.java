package com.alfgoto.rune.gameplay.crafting;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * Clicking a Rune recipe in the vanilla recipe book puts the right Rune items in the grid. Vanilla auto-fill only
 * knows the base material, so it can take any Rune helmet for any helmet recipe. For a Rune recipe the vanilla fill is
 * cancelled: the grid goes back to the inventory and is filled with {@link RecipeBook#fill}. A missing ingredient
 * changes nothing. Vanilla recipes are not touched.
 */
public final class RecipeBookClickListener implements Listener {

    private final Server server;
    private final RecipeBook book;

    public RecipeBookClickListener(Server server, RecipeBook book) {
        this.server = server;
        this.book = book;
    }

    @EventHandler
    public void onClick(PlayerRecipeBookClickEvent event) {
        Recipe recipe = server.getRecipe(event.getRecipe());
        CraftingRecipe runeRecipe = recipe == null ? null : book.of(recipe).orElse(null);
        if (runeRecipe == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!(player.getOpenInventory().getTopInventory() instanceof CraftingInventory grid)) {
            return;
        }
        int side = (int) Math.round(Math.sqrt(grid.getMatrix().length));
        // Work on the inventory without the grid items: they go back first, as vanilla does.
        ItemStack[] storage = player.getInventory().getStorageContents();
        ItemStack[] returned = grid.getMatrix();
        ItemStack[] withGrid = storage.clone();
        for (ItemStack stack : returned) {
            if (stack != null && !stack.getType().isAir() && !put(withGrid, stack)) {
                return; // no room to put the grid back: change nothing
            }
        }
        book.fill(runeRecipe, withGrid, side, event.isMakeAll()).ifPresent(fill -> {
            player.getInventory().setStorageContents(fill.storage());
            grid.setMatrix(fill.grid());
            player.updateInventory();
        });
    }

    /** Adds the stack to the first slots that take it (merging with the same item). False when it does not all fit. */
    private static boolean put(ItemStack[] storage, ItemStack stack) {
        ItemStack rest = stack.clone();
        for (int i = 0; i < storage.length && rest.getAmount() > 0; i++) {
            ItemStack there = storage[i];
            if (there != null && !there.getType().isAir() && there.isSimilar(rest)) {
                int moved = Math.min(rest.getAmount(), there.getMaxStackSize() - there.getAmount());
                if (moved > 0) {
                    ItemStack merged = there.clone();
                    merged.setAmount(there.getAmount() + moved);
                    storage[i] = merged;
                    rest.setAmount(rest.getAmount() - moved);
                }
            }
        }
        for (int i = 0; i < storage.length && rest.getAmount() > 0; i++) {
            if (storage[i] == null || storage[i].getType().isAir()) {
                storage[i] = rest.clone();
                rest.setAmount(0);
            }
        }
        return rest.getAmount() <= 0;
    }
}
