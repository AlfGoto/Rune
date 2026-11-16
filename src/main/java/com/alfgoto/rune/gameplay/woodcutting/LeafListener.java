package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.woodcutting.LeafRules.Offset;
import java.util.List;
import java.util.function.DoubleSupplier;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Leaf Fall and Orchard, for a player who breaks a block with a Rune Tool.
 *
 * <ul>
 *   <li>Leaf Fall: breaking a log makes the leaves around it fall at once, with their normal drops. They are
 *       broken without a break event, so they give 0 XP.
 *   <li>Orchard: each leaf the player breaks, or makes fall, can roll its vanilla loot again and keep only the
 *       apples and saplings.
 * </ul>
 */
final class LeafListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final DoubleSupplier random;

    LeafListener(ItemRegistry items, ToolLoadout loadout, DoubleSupplier random) {
        this.items = items;
        this.loadout = loadout;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty()) {
            return;
        }
        List<Stacking.Source> sources = loadout.sources(tool);
        Block block = event.getBlock();
        if (Tag.LEAVES.isTagged(block.getType())) {
            // Leaves a player placed give no bonus, same as for Leaf Fall.
            if (block.getBlockData() instanceof Leaves data && !data.isPersistent()) {
                orchard(player, tool, sources, block);
            }
        } else if (Tag.LOGS.isTagged(block.getType())) {
            leafFall(player, tool, sources, block);
        }
    }

    private void leafFall(Player player, ItemStack tool, List<Stacking.Source> sources, Block log) {
        double radius = Stacking.total(LeafRules.LEAF_FALL, sources);
        if (radius < 1) {
            return;
        }
        // The Silk Touch Rune is not an enchantment, so the leaves that fall get a Silk Touch copy of the tool.
        ItemStack breaker = null;
        if (Stacking.total(Stacking.SILK_TOUCH, sources) > 0) {
            breaker = tool.clone();
            breaker.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        }
        for (Offset offset : LeafRules.offsets(radius)) {
            Block leaves = log.getRelative(offset.x(), offset.y(), offset.z());
            // Leaves a player placed do not decay in vanilla, so they do not fall either.
            if (leaves.getBlockData() instanceof Leaves data && !data.isPersistent()) {
                orchard(player, tool, sources, leaves);
                if (breaker == null) {
                    leaves.breakNaturally();
                } else {
                    leaves.breakNaturally(breaker);
                }
            }
        }
    }

    /** Rolls the vanilla loot of {@code leaves} again for each Orchard roll, and drops only apples and saplings. */
    private void orchard(Player player, ItemStack tool, List<Stacking.Source> sources, Block leaves) {
        if (Stacking.total(Stacking.SILK_TOUCH, sources) > 0) {
            return; // Silk Touch drops the leaves themselves
        }
        int rolls = Stacking.rolls(Stacking.total(LeafRules.ORCHARD, sources), random);
        for (int i = 0; i < rolls; i++) {
            for (ItemStack drop : leaves.getDrops(tool, player)) {
                if (isOrchardDrop(drop.getType())) {
                    leaves.getWorld().dropItemNaturally(leaves.getLocation().add(0.5, 0.5, 0.5), drop);
                }
            }
        }
    }

    static boolean isOrchardDrop(Material type) {
        return type == Material.APPLE || Tag.SAPLINGS.isTagged(type);
    }
}
