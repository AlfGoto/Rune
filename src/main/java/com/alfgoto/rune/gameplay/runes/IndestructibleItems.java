package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * A player can't lose a Rune for now: dropped Rune items, Rune Tools and Rune Armor pieces take no
 * damage (fire, lava, cactus, explosions, lightning) and never despawn. Falling into the void
 * teleports them to the world spawn. They can still be dropped, picked up and kept in chests.
 */
public final class IndestructibleItems implements Listener {

    private final ItemRegistry items;
    private final RuneRegistry runes;
    private final List<Predicate<ItemStack>> extra = new CopyOnWriteArrayList<>();

    public IndestructibleItems(ItemRegistry items, RuneRegistry runes) {
        this.items = items;
        this.runes = runes;
    }

    /** Protects more items (e.g. Rune Caches, which hold Runes). */
    public void protect(Predicate<ItemStack> predicate) {
        extra.add(predicate);
    }

    /** True for a Rune, a Rune Tool or a Rune Armor piece (they carry Runes). */
    public boolean isProtected(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        if (runes.of(stack).isPresent()) {
            return true;
        }
        if (extra.stream().anyMatch(p -> p.test(stack))) {
            return true;
        }
        return items.of(stack)
                .filter(item -> item.toolSpec().isPresent() || item.armorSpec().isPresent())
                .isPresent();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawn(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        if (isProtected(entity.getItemStack())) {
            entity.setUnlimitedLifetime(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDespawn(ItemDespawnEvent event) {
        if (isProtected(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item) || !isProtected(item.getItemStack())) {
            return;
        }
        event.setCancelled(true);
        if (event.getCause() == DamageCause.VOID) {
            Location spawn = item.getWorld().getSpawnLocation().add(0.5, 0, 0.5);
            item.setVelocity(new Vector());
            item.teleport(spawn);
        }
    }
}
