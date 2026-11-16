package com.alfgoto.rune.gameplay.drops;

import com.alfgoto.rune.gameplay.drops.DropRules.Mode;
import com.alfgoto.rune.gameplay.drops.DropRules.Stack;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Applies Fortune, Smelt and Silk Touch to the drops of a block a player breaks with a Rune Tool. The rules are in
 * {@link DropRules}; the Runes installed on the tool are read with {@link ToolLoadout#sources}.
 */
public final class DropEffectsListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final Smelting smelting;
    private final DoubleSupplier random;

    public DropEffectsListener(ItemRegistry items, ToolLoadout loadout, Smelting smelting, DoubleSupplier random) {
        this.items = items;
        this.loadout = loadout;
        this.smelting = smelting;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrops(BlockDropItemEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty()) {
            return;
        }
        List<Stacking.Source> sources = loadout.sources(tool);
        switch (DropRules.mode(sources)) {
            case UNCHANGED -> {}
            case SILK -> {} // handled in onBreak
            case CHANGED -> change(event, sources);
        }
    }

    /**
     * Silk Touch: the block drops what a vanilla Silk Touch version of the held tool would give. The vanilla drops
     * are turned off and made here, so blocks that vanilla drops nothing for (glass) still work. The copy of the
     * tool keeps its own Harvest Tier.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty() || DropRules.mode(loadout.sources(tool)) != Mode.SILK) {
            return;
        }
        ItemStack silkTool = tool.clone();
        silkTool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        Block block = event.getBlock();
        var wanted = block.getDrops(silkTool, event.getPlayer());
        event.setDropItems(false);
        for (ItemStack stack : wanted) {
            block.getWorld().dropItemNaturally(block.getLocation(), stack);
        }
    }

    private void change(BlockDropItemEvent event, List<Stacking.Source> sources) {
        List<Item> dropped = event.getItems();
        List<Stack<Material>> stacks = new ArrayList<>();
        for (Item item : dropped) {
            stacks.add(new Stack<>(item.getItemStack().getType(), item.getItemStack().getAmount()));
        }
        List<Stack<Material>> result = DropRules.apply(sources, stacks, smelting::smelt, random);
        for (int i = 0; i < dropped.size(); i++) {
            ItemStack original = dropped.get(i).getItemStack();
            Stack<Material> changed = result.get(i);
            if (original.getType() == changed.item() && original.getAmount() == changed.amount()) {
                continue; // nothing changed: keep the stack and its data
            }
            if (original.getType() == changed.item()) {
                // Same item, new amount: keep the data (shulker contents, banner patterns...).
                ItemStack copy = original.clone();
                copy.setAmount(changed.amount());
                dropped.get(i).setItemStack(copy);
            } else {
                // Smelted into another item: the old item's data does not apply.
                dropped.get(i).setItemStack(new ItemStack(changed.item(), changed.amount()));
            }
        }
    }
}
