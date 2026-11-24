package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleSupplier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The Trait extras that fire when the player fells a tree with the axe in hand:
 *
 * <ul>
 *   <li>Birch Axe: a Speed boost for a few seconds.
 *   <li>Poplar Axe: the leaves of the tree fall at once, with their normal drops. They are broken without a
 *       break event, so they give 0 XP (same as Leaf Fall).
 * </ul>
 */
final class FelledTreeTraitListener implements Listener {

    private static final BlockFace[] SIDES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    /** Leaves broken per tick (the canopy falls over a few ticks, not all at once). */
    private static final int LEAVES_PER_TICK = 40;

    private final Plugin plugin;
    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final int maxLeaves;
    private final int leafReach;
    private final DoubleSupplier random;

    /**
     * @param maxLeaves most leaves one Felled Tree can make fall
     * @param leafReach how far from the last log (in blocks) a leaf can still fall
     */
    FelledTreeTraitListener(Plugin plugin, ItemRegistry items, ToolLoadout loadout, int maxLeaves, int leafReach, DoubleSupplier random) {
        this.random = random;
        this.plugin = plugin;
        this.items = items;
        this.loadout = loadout;
        this.maxLeaves = maxLeaves;
        this.leafReach = leafReach;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFelled(TreeFelled event) {
        ItemStack tool = event.item();
        if (tool == null || items.of(tool).isEmpty()) {
            return;
        }
        Player player = event.player();
        List<Stacking.Source> sources = loadout.sources(tool, player);
        double seconds = Stacking.total(AxeTraits.FELLED_TREE_SPEED, sources);
        if (seconds > 0) {
            int ticks = (int) Math.min(Integer.MAX_VALUE, Math.round(seconds * 20));
            PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
            // Never replace a stronger Speed (potion, beacon) or a longer one of the same level.
            if (current == null || (current.getAmplifier() == 0 && current.getDuration() < ticks)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 0));
            }
        }
        if (Stacking.total(AxeTraits.FELLED_TREE_LEAF_FALL, sources) >= 1) {
            leavesFall(event.lastLog(), player, tool.clone(), sources);
        }
    }

    private void leavesFall(Block lastLog, Player player, ItemStack tool, List<Stacking.Source> sources) {
        List<Block> leaves = AxeTraits.connectedLeaves(
                lastLog,
                block -> java.util.Arrays.stream(SIDES).map(block::getRelative).toList(),
                // Leaves a player placed do not decay in vanilla, so they do not fall either.
                block -> block.getBlockData() instanceof Leaves data && !data.isPersistent(),
                leafReach,
                maxLeaves);
        // Break them a few per tick, so a big canopy does not freeze the server.
        Iterator<Block> pending = leaves.iterator();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < LEAVES_PER_TICK && pending.hasNext(); i++) {
                    Block block = pending.next();
                    // Vanilla decay may have removed it already.
                    if (block.getBlockData() instanceof Leaves) {
                        LeafListener.orchard(random, player, tool, sources, block);
                        block.breakNaturally();
                    }
                }
                if (!pending.hasNext()) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
