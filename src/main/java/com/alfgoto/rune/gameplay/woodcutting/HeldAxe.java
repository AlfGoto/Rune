package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Reads a Trait effect from the Rune Tool in a player's main hand. A Broken tool gives nothing. */
final class HeldAxe {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final Predicate<ItemStack> isBroken;

    HeldAxe(ItemRegistry items, ToolLoadout loadout, Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.isBroken = isBroken;
    }

    /** The total of {@code effectId} on the main hand tool, or 0 when it is not a working Rune Tool. */
    double value(Player player, String effectId) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || items.of(tool).isEmpty() || isBroken.test(tool)) {
            return 0;
        }
        return Stacking.total(effectId, loadout.sources(tool, player));
    }
}
