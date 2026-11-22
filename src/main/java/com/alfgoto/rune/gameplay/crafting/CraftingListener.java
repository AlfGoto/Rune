package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * A locked recipe cannot be crafted: the result is removed and the craft is cancelled.
 * An unlocked Rune recipe gets a fresh result, so it goes through the redrawers (name, lore) like /rune give.
 *
 * <p>Upgrade: when the ingredients are Rune gear with installed Runes, the result keeps the Runes that fit its
 * slots and Capacity. The rest go to the player's inventory, or drop on the ground if it is full. Only a plain
 * left or right click is allowed for an Upgrade that has Runes to carry over: shift-click would craft many
 * results, and number keys or drop would move the result without a clean single craft.
 */
public final class CraftingListener implements Listener {

    private final RecipeBook book;
    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final RuneRegistry runes;

    public CraftingListener(RecipeBook book, ItemRegistry items, ToolLoadout loadout, RuneRegistry runes) {
        this.book = book;
        this.items = items;
        this.loadout = loadout;
        this.runes = runes;
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        CraftingRecipe recipe = book.of(event.getRecipe()).orElse(null);
        if (recipe == null) {
            return;
        }
        if (!book.hasRuneIngredients(recipe, event.getInventory().getMatrix())) {
            event.getInventory().setResult(null); // right material, wrong Rune item (e.g. a vanilla helmet)
        } else if (book.isUnlocked(event.getView().getPlayer().getUniqueId(), recipe)) {
            ItemStack result = items.create(items.get(recipe.result()), recipe.amount());
            carryOver(event.getInventory(), result);
            event.getInventory().setResult(result);
        } else {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        CraftingRecipe recipe = book.of(event.getRecipe()).orElse(null);
        if (recipe == null) {
            return;
        }
        HumanEntity who = event.getWhoClicked();
        if (!book.hasRuneIngredients(recipe, event.getInventory().getMatrix())
                || !book.isUnlocked(who.getUniqueId(), recipe)) {
            event.setCancelled(true);
            return;
        }
        ItemStack result = items.create(items.get(recipe.result()), recipe.amount());
        if (carryOver(event.getInventory(), result) == null) {
            return; // nothing to carry over
        }
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
            event.setCancelled(true);
            return;
        }
        event.setCurrentItem(result);
    }

    /**
     * Gives back the Runes that did not fit, only when the craft really happens (no other listener cancelled
     * it), so a cancelled craft never duplicates Runes. The grid is still full at this point.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftDone(CraftItemEvent event) {
        CraftingRecipe recipe = book.of(event.getRecipe()).orElse(null);
        if (recipe == null) {
            return;
        }
        ItemStack result = items.create(items.get(recipe.result()), recipe.amount());
        List<ToolLoadout.Installed> overflow = carryOver(event.getInventory(), result);
        if (overflow == null) {
            return;
        }
        HumanEntity who = event.getWhoClicked();
        for (ToolLoadout.Installed back : overflow) {
            ItemStack stack = runes.create(back.rune(), back.rank());
            who.getInventory().addItem(stack).values()
                    .forEach(left -> who.getWorld().dropItemNaturally(who.getLocation(), left));
        }
    }

    /**
     * Installs on {@code result} the Runes of the Rune gear in the grid. Returns the Runes that did not fit (all of them when the result is not a Rune Tool), or
     * null when there is nothing to carry over.
     */
    private List<ToolLoadout.Installed> carryOver(CraftingInventory inventory, ItemStack result) {
        List<ItemStack> sources = new ArrayList<>();
        for (ItemStack stack : inventory.getMatrix()) {
            if (stack != null && !loadout.installed(stack).isEmpty()) {
                sources.add(stack);
            }
        }
        if (sources.isEmpty()) {
            return null;
        }
        return loadout.inherit(result, sources);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        book.sync(event.getPlayer());
    }
}
