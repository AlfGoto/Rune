package com.alfgoto.rune.gameplay.items;

import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * The Rune Codex (docs/design-decisions.md, UI): every player carries one, soulbound, in the last
 * inventory slot (not the hotbar). {@link CodexListener} keeps it there.
 */
public final class Codex {

    /** Item id of the Rune Codex in the {@link ItemRegistry}. */
    public static final String ID = "rune_codex";

    /** The last slot of the player inventory (the top-left slot of the main grid is 9, the last is 35). */
    public static final int SLOT = 35;

    private final ItemRegistry items;

    public Codex(ItemRegistry items) {
        this.items = items;
    }

    public boolean is(ItemStack stack) {
        return items.of(stack).filter(item -> item.id().equals(ID)).isPresent();
    }

    /**
     * Puts the Codex in its slot. Other Codex stacks are removed, and what was in the slot moves to
     * another free slot (or drops at the player's feet).
     */
    public void ensure(Player player) {
        if (items.find(ID).isEmpty()) {
            return; // content did not register the Codex: nothing to give
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack inSlot = inventory.getItem(SLOT);
        if (is(inSlot)) {
            removeOthers(inventory);
            return;
        }
        removeOthers(inventory);
        inventory.setItem(SLOT, null);
        if (inSlot != null && !inSlot.getType().isAir()) {
            Map<Integer, ItemStack> leftover = inventory.addItem(inSlot);
            leftover.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
        }
        inventory.setItem(SLOT, items.create(items.get(ID), 1));
    }

    private void removeOthers(PlayerInventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot != SLOT && is(inventory.getItem(slot))) {
                inventory.setItem(slot, null);
            }
        }
    }
}
