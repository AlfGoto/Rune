package com.alfgoto.rune.gameplay.durability;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.function.DoubleSupplier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Lowers the durability a Rune Tool loses when Sturdy is installed (or a Trait gives it). It runs before the
 * Broken state check, which reads the lowered damage.
 */
public final class SturdyListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final DoubleSupplier random;

    public SturdyListener(ItemRegistry items, ToolLoadout loadout, DoubleSupplier random) {
        this.items = items;
        this.loadout = loadout;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack tool = event.getItem();
        if (items.of(tool).filter(item -> item.toolSpec().isPresent()).isEmpty()) {
            return;
        }
        double percent = Stacking.total(Sturdy.ID, loadout.sources(tool, event.getPlayer()));
        if (percent == 0) {
            return;
        }
        int damage = Sturdy.damage(event.getDamage(), percent, random);
        if (damage == 0) {
            event.setCancelled(true);
        } else {
            event.setDamage(damage);
        }
    }
}
