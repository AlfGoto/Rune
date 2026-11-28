package com.alfgoto.rune.gameplay.drops;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.Set;
import java.util.function.DoubleSupplier;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Bonus gems (Geode Pickaxe Trait): when a block drops a gem, there is a chance of +1 gem. Blocks the player placed
 * give nothing extra. A Silk Touch drop is the block itself, not a gem, so it gets no bonus.
 */
public final class BonusGemsListener implements Listener {

    /** Effect id: chance of +1 gem, in percent (above 100% gives several). */
    public static final String ID = "bonus_gems";

    /** The items counted as gems. */
    static final Set<Material> GEMS = Set.of(
            Material.DIAMOND, Material.EMERALD, Material.LAPIS_LAZULI, Material.QUARTZ, Material.AMETHYST_SHARD);

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlockTracker tracker;
    private final DoubleSupplier random;

    /** The block just broken, if it was tagged {@code placed}. The drop event follows the break event at once. */
    private Block placedBreak;

    public BonusGemsListener(ItemRegistry items, ToolLoadout loadout, BlockTracker tracker, DoubleSupplier random) {
        this.items = items;
        this.loadout = loadout;
        this.tracker = tracker;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        // Read the tag here: Block Tracking clears it at MONITOR, before the drop event.
        placedBreak = tracker.has(event.getBlock(), BlockTag.PLACED) ? event.getBlock() : null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrops(BlockDropItemEvent event) {
        boolean placed = placedBreak != null && placedBreak.equals(event.getBlock());
        placedBreak = null;
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (placed || items.of(tool).isEmpty()) {
            return;
        }
        double chance = Stacking.total(ID, loadout.sources(tool, event.getPlayer()));
        if (chance <= 0) {
            return;
        }
        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            if (GEMS.contains(stack.getType())) {
                int bonus = Stacking.rolls(chance, random);
                if (bonus > 0) {
                    ItemStack copy = stack.clone();
                    copy.setAmount(stack.getAmount() + bonus);
                    item.setItemStack(copy);
                }
                return; // one roll per block, on the first gem stack
            }
        }
    }
}
