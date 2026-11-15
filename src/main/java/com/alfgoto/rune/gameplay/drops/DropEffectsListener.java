package com.alfgoto.rune.gameplay.drops;

import com.alfgoto.rune.gameplay.drops.DropRules.Mode;
import com.alfgoto.rune.gameplay.drops.DropRules.Stack;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import org.bukkit.Material;
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
    private final Map<UUID, List<ItemStack>> silkDrops = new HashMap<>();

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
            case SILK -> silk(event);
            case CHANGED -> change(event, sources);
        }
    }

    /**
     * Just before the block breaks it still exists, so ask what a vanilla Silk Touch tool would drop. The drop
     * event comes right after and uses the answer (the block is air by then).
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        silkDrops.remove(event.getPlayer().getUniqueId());
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty() || DropRules.mode(loadout.sources(tool)) != Mode.SILK) {
            return;
        }
        ItemStack silkTool = new ItemStack(Material.NETHERITE_PICKAXE);
        silkTool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
        silkDrops.put(
                event.getPlayer().getUniqueId(),
                new ArrayList<>(event.getBlock().getDrops(silkTool, event.getPlayer())));
    }

    /** The block drops itself: what a vanilla Silk Touch pickaxe would give. */
    private void silk(BlockDropItemEvent event) {
        List<ItemStack> wanted = silkDrops.remove(event.getPlayer().getUniqueId());
        if (wanted == null || wanted.isEmpty()) {
            return; // nothing to put back (e.g. a block that never drops)
        }
        List<Item> dropped = event.getItems();
        int shared = Math.min(dropped.size(), wanted.size());
        for (int i = 0; i < shared; i++) {
            dropped.get(i).setItemStack(wanted.get(i));
        }
        // More vanilla drops than silk drops: the extra ones are not dropped.
        dropped.subList(shared, dropped.size()).clear();
        // Fewer vanilla drops than silk drops: drop the rest.
        for (int i = shared; i < wanted.size(); i++) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), wanted.get(i));
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
            dropped.get(i).setItemStack(new ItemStack(result.get(i).item(), result.get(i).amount()));
        }
    }
}
