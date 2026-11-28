package com.alfgoto.rune.gameplay.drops;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Magnet: the drops of a block broken with a Rune Tool go straight into the inventory, and so do the dropped items
 * lying within the Magnet radius of the block (Rune Caches and drops of a multi-block break). What does not fit
 * stays on the ground. Items a player threw are left alone.
 */
public final class MagnetListener implements Listener {

    /** Effect id of Magnet: the pull radius, in blocks. */
    public static final String ID = "magnet";

    /** Effect id of the Ender Trait: on/off. Any value above 0 sends the block's drops to the Ender Chest. */
    public static final String ENDER = "ender_drops";

    private final ItemRegistry items;
    private final ToolLoadout loadout;

    public MagnetListener(ItemRegistry items, ToolLoadout loadout) {
        this.items = items;
        this.loadout = loadout;
    }

    // After Fortune and Smelt (HIGH), so the changed drops are the ones pulled.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrops(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty()) {
            return;
        }
        var sources = loadout.sources(tool, player);
        if (Stacking.total(ENDER, sources) > 0) {
            pull(player.getEnderChest(), event.getItems().iterator()); // what does not fit stays on the ground
        }
        double radius = Stacking.total(ID, sources);
        if (radius <= 0) {
            return;
        }
        pull(player.getInventory(), event.getItems().iterator());
        Collection<Item> nearby = event.getBlock()
                .getWorld()
                .getNearbyEntities(event.getBlock().getLocation().add(0.5, 0.5, 0.5), radius, radius, radius)
                .stream()
                .filter(Item.class::isInstance)
                .map(Item.class::cast)
                .filter(item -> item.getThrower() == null)
                .toList();
        pull(player.getInventory(), nearby.iterator());
    }

    /** Puts each item in the inventory. Removes it when it all fits, else it keeps the part that did not fit. */
    private static void pull(Inventory inventory, Iterator<Item> iterator) {
        while (iterator.hasNext()) {
            Item item = iterator.next();
            Map<Integer, ItemStack> left = inventory.addItem(item.getItemStack());
            if (left.isEmpty()) {
                if (item.isValid()) {
                    item.remove(); // already in the world: take it away
                } else {
                    iterator.remove(); // still in the event's list: it is never dropped
                }
            } else {
                item.setItemStack(left.values().iterator().next());
            }
        }
    }
}
