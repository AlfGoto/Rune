package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
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
 * slots and Capacity. The rest go to the player's inventory, or drop on the ground if it is full. Shift-click
 * is refused for an Upgrade that has Runes to carry over, because one click would craft many results.
 */
public final class CraftingListener implements Listener {

    private final RecipeBook book;
    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final RuneRegistry runes;

    public CraftingListener(RecipeBook book, ItemRegistry items) {
        this(book, items, null, null);
    }

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
        if (book.isUnlocked(event.getView().getPlayer().getUniqueId(), recipe)) {
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
        if (!book.isUnlocked(who.getUniqueId(), recipe)) {
            event.setCancelled(true);
            return;
        }
        ItemStack result = items.create(items.get(recipe.result()), recipe.amount());
        List<ToolLoadout.Installed> overflow = carryOver(event.getInventory(), result);
        if (overflow == null) {
            return; // nothing to carry over
        }
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            event.setCancelled(true);
            return;
        }
        event.setCurrentItem(result);
        for (ToolLoadout.Installed back : overflow) {
            ItemStack stack = runes.create(back.rune(), back.rank());
            who.getInventory().addItem(stack).values()
                    .forEach(left -> who.getWorld().dropItemNaturally(who.getLocation(), left));
        }
    }

    /**
     * Installs on {@code result} the Runes of the Rune gear in the grid. Returns the Runes that did not fit, or
     * null when there is nothing to carry over.
     */
    private List<ToolLoadout.Installed> carryOver(CraftingInventory inventory, ItemStack result) {
        if (loadout == null || loadout.stats(result).isEmpty()) {
            return null;
        }
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
