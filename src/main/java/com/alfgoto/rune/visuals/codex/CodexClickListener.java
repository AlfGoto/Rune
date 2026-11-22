package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.items.Codex;
import com.alfgoto.rune.visuals.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

/** Clicking the Rune Codex in the inventory screen opens the main menu. */
final class CodexClickListener implements Listener {

    private final Codex codex;
    private final CodexMenus menus;

    CodexClickListener(Codex codex, CodexMenus menus) {
        this.codex = codex;
        this.menus = menus;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !(event.getClickedInventory() instanceof PlayerInventory)
                || event.getSlot() != Codex.SLOT
                || !codex.is(event.getCurrentItem())
                || !(event.isLeftClick() || event.isRightClick())
                || event.getView().getTopInventory().getHolder() instanceof Menu) {
            return;
        }
        event.setCancelled(true);
        menus.main(player).open(player);
        // The client already "picked up" the Codex on click. Cancelling does not clear that cursor
        // on the client once a menu opens, so the Codex shows in hand after closing. Resync it.
        player.setItemOnCursor(null);
        player.updateInventory();
    }
}
