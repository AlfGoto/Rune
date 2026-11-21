package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final int speedLevel;

    /** @param speedLevel the Speed level of the Birch boost (1 = Speed I) */
    FelledTreeTraitListener(ItemRegistry items, ToolLoadout loadout, int speedLevel) {
        this.items = items;
        this.loadout = loadout;
        this.speedLevel = Math.max(1, speedLevel);
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, speedLevel - 1));
        }
        if (Stacking.total(AxeTraits.FELLED_TREE_LEAF_FALL, sources) >= 1) {
            leavesFall(event.lastLog());
        }
    }

    private static void leavesFall(Block lastLog) {
        List<Block> leaves = AxeTraits.connectedLeaves(
                lastLog,
                block -> java.util.Arrays.stream(SIDES).map(block::getRelative).toList(),
                // Leaves a player placed do not decay in vanilla, so they do not fall either.
                block -> block.getBlockData() instanceof Leaves data && !data.isPersistent(),
                AxeTraits.LEAF_REACH,
                AxeTraits.MAX_FALLING_LEAVES);
        for (Block block : leaves) {
            block.breakNaturally();
        }
    }
}
