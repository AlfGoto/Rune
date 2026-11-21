package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Replant: when the player fells a tree with a Rune Tool that has the Rune (or the Trait), a sapling of the
 * same wood type is planted where the tree stood. It is free, and it is skipped where the sapling could not
 * stand (no soil, something in the way). The sapling is planted one tick later because the last log is still
 * in the world while {@link TreeFelled} runs. Planting does not fire a block place event, so it is not a
 * player-placed block.
 */
final class ReplantListener implements Listener {

    private final Plugin plugin;
    private final ItemRegistry items;
    private final ToolLoadout loadout;

    ReplantListener(Plugin plugin, ItemRegistry items, ToolLoadout loadout) {
        this.plugin = plugin;
        this.items = items;
        this.loadout = loadout;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTreeFelled(TreeFelled event) {
        ItemStack tool = event.item();
        if (items.of(tool).isEmpty() || Stacking.total(ReplantRules.REPLANT, loadout.sources(tool)) < 1) {
            return;
        }
        List<Block> bases = event.bases();
        if (bases.isEmpty()) {
            return;
        }
        // The wood type of the tree, read now: the logs are gone next tick.
        Optional<Material> sapling = ReplantRules.saplingFor(bases.get(0).getType());
        if (sapling.isEmpty()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (Block base : bases) {
                plant(base, sapling.get());
            }
        });
    }

    private static void plant(Block base, Material sapling) {
        if (!base.getType().isAir() && !base.isReplaceable()) {
            return;
        }
        BlockData data = sapling.createBlockData();
        if (data.isSupported(base)) {
            base.setBlockData(data);
        }
    }
}
