package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Jungle Axe: while it is in the main hand, the player climbs faster on vines, ladders and scaffolding.
 * Climbing speed is not an attribute, so each move up a climbable block sets the upward velocity to the
 * boosted speed.
 */
final class ClimbListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;

    ClimbListener(ItemRegistry items, ToolLoadout loadout) {
        this.items = items;
        this.loadout = loadout;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo().getY() <= event.getFrom().getY()) {
            return;
        }
        Player player = event.getPlayer();
        if (!Tag.CLIMBABLE.isTagged(player.getLocation().getBlock().getType())) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty()) {
            return;
        }
        double percent = Stacking.total(AxeTraits.CLIMB_SPEED, loadout.sources(tool, player));
        if (percent <= 0) {
            return;
        }
        Vector velocity = player.getVelocity();
        player.setVelocity(velocity.setY(Math.max(velocity.getY(), AxeTraits.climbSpeed(percent))));
    }
}
