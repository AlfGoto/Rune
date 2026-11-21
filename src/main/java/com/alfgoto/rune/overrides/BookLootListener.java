package com.alfgoto.rune.overrides;

import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.text.Rarity;
import com.destroystokyo.paper.event.entity.EntityZapEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

/**
 * Enchanted books never come from loot: chests ({@code LootGenerateEvent}), fishing and villager trades give a
 * Rune Cache instead (the amount is kept, the Rarity comes from the config).
 */
public class BookLootListener implements Listener {

    private final Caches caches;
    private final Rarity rarity;

    public BookLootListener(Caches caches, Rarity rarity) {
        this.caches = caches;
        this.rarity = rarity;
    }

    @EventHandler
    public void onLoot(LootGenerateEvent event) {
        List<ItemStack> loot = event.getLoot();
        loot.replaceAll(this::replace);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item item) {
            item.setItemStack(replace(item.getItemStack()));
        }
    }

    @EventHandler
    public void onAcquireTrade(VillagerAcquireTradeEvent event) {
        event.setRecipe(replace(event.getRecipe()));
    }

    /** The stack, or a Cache of the same amount when it is an enchanted book. */
    ItemStack replace(ItemStack stack) {
        if (stack == null || stack.getType() != Material.ENCHANTED_BOOK) {
            return stack;
        }
        ItemStack cache = caches.create(rarity, null);
        cache.setAmount(stack.getAmount());
        return cache;
    }

    /** The recipe, or a copy with a Cache as result when it sells an enchanted book. */
    MerchantRecipe replace(MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        if (result.getType() != Material.ENCHANTED_BOOK) {
            return recipe;
        }
        MerchantRecipe copy = new MerchantRecipe(
                replace(result),
                recipe.getUses(),
                recipe.getMaxUses(),
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier(),
                recipe.getDemand(),
                recipe.getSpecialPrice(),
                recipe.shouldIgnoreDiscounts());
        copy.setIngredients(recipe.getIngredients());
        return copy;
    }
}
