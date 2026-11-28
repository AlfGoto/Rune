package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Warped Axe: while it is in the main hand, an enderman that is looked at does not turn hostile. Endermen
 * that are already angry (the player hit one) stay angry.
 */
final class EndermanListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final Predicate<ItemStack> isBroken;

    EndermanListener(ItemRegistry items, ToolLoadout loadout, Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.isBroken = isBroken;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLookedAt(EndermanAttackPlayerEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty() || isBroken.test(tool)) {
            return;
        }
        if (Stacking.total(AxeTraits.ENDERMEN_NEUTRAL, loadout.sources(tool, player)) > 0) {
            event.setCancelled(true);
        }
    }
}
