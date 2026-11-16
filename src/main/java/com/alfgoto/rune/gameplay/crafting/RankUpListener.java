package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.text.Text;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.plugin.Plugin;

/**
 * Rank-up at the vanilla smithing table: 3 Runes in the 3 input slots show the next Rank in the output slot,
 * with the cost in its lore. Taking it costs vanilla XP levels and uses one Rune from each slot.
 *
 * <p>The smithing table only accepts items that some smithing recipe accepts, so one hidden smithing recipe
 * accepts every material a Rune is built on in all 3 slots. Its result is never shown: this listener
 * replaces it. The output slot is handled by hand, because vanilla would otherwise decide what can be taken.
 */
public final class RankUpListener implements Listener {

    private final Plugin plugin;
    private final RankUp rankUp;
    private final java.util.function.Supplier<Set<Material>> runeMaterials;
    private NamespacedKey recipeKey;

    /** {@code runeMaterials} gives the vanilla materials of all Runes; read when the recipe is registered. */
    public RankUpListener(Plugin plugin, RankUp rankUp, java.util.function.Supplier<Set<Material>> runeMaterials) {
        this.plugin = plugin;
        this.rankUp = rankUp;
        this.runeMaterials = runeMaterials;
    }

    /** Registers the hidden smithing recipe. Call it once every Rune is registered. */
    public void registerRecipe() {
        Set<Material> materials = new LinkedHashSet<>(runeMaterials.get());
        if (materials.isEmpty()) {
            return;
        }
        recipeKey = new NamespacedKey(plugin, "rank_up");
        RecipeChoice choice = new RecipeChoice.MaterialChoice(materials.toArray(Material[]::new));
        Bukkit.addRecipe(new SmithingTransformRecipe(recipeKey, new ItemStack(Material.PAPER), choice, choice, choice));
    }

    public void unregisterRecipe() {
        if (recipeKey != null) {
            Bukkit.removeRecipe(recipeKey);
            recipeKey = null;
        }
    }

    @EventHandler
    public void onPrepare(PrepareSmithingEvent event) {
        SmithingInventory inventory = event.getInventory();
        RankUp.Plan plan = plan(inventory);
        if (plan.refusal() == RankUp.Refusal.NOT_RUNES) {
            return; // not ours: vanilla (and other plugins) decide
        }
        if (!plan.valid()) {
            event.setResult(null);
            return;
        }
        int levels = event.getView().getPlayer() instanceof Player p ? p.getLevel() : 0;
        event.setResult(rankUp.preview(plan, levels));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof SmithingInventory inventory)
                || event.getSlotType() != InventoryType.SlotType.RESULT
                || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        RankUp.Plan plan = plan(inventory);
        if (plan.refusal() == RankUp.Refusal.NOT_RUNES) {
            return;
        }
        event.setCancelled(true);
        if (plan.refusal() == RankUp.Refusal.DIFFERENT) {
            say(player, "rune.rank_up.different", "The 3 Runes must be the same Rune at the same Rank.");
            return;
        }
        if (plan.refusal() == RankUp.Refusal.MAX_RANK) {
            say(player, "rune.rank_up.max_rank", "This Rune is already at its Max Rank.");
            return;
        }
        // Simplest rule: a plain click with an empty cursor. No shift-click, no number keys, no drop.
        if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
            return;
        }
        if (!event.getCursor().isEmpty()) {
            say(player, "rune.rank_up.cursor_full", "Empty your cursor first.");
            return;
        }
        if (player.getLevel() < plan.cost()) {
            player.sendMessage(Text.translatable(
                            "rune.rank_up.not_enough", "You need %s XP levels.", Component.text(plan.cost()))
                    .color(NamedTextColor.RED));
            return;
        }
        player.giveExpLevels(-plan.cost());
        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getAmount() <= 1) {
                inventory.setItem(slot, null);
            } else {
                stack.setAmount(stack.getAmount() - 1);
                inventory.setItem(slot, stack);
            }
        }
        player.setItemOnCursor(rankUp.result(plan));
        player.sendMessage(Text.translatable(
                "rune.rank_up.done",
                "Ranked up %s to Rank %s for %s XP levels.",
                Component.translatable(Text.key("item", plan.rune().id())),
                Component.text(plan.fromRank() + 1),
                Component.text(plan.cost())));
        // Recompute the output slot now that the inputs changed.
        inventory.setResult(null);
        player.updateInventory();
    }

    private RankUp.Plan plan(SmithingInventory inventory) {
        return rankUp.plan(inventory.getItem(0), inventory.getItem(1), inventory.getItem(2));
    }

    private static void say(Player player, String key, String english) {
        player.sendMessage(Text.translatable(key, english).color(NamedTextColor.RED));
    }
}
