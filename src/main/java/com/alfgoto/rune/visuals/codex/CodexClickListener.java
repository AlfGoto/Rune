package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.items.Codex;
import com.alfgoto.rune.visuals.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

/**
 * Clicking the Rune Codex in the inventory screen opens the main menu, one tick later.
 *
 * <p>Opening an inventory inside an {@link InventoryClickEvent} swaps the player's container in
 * the middle of the click. The server then finishes the click (resync of the cancelled click,
 * remote updates turned back on) on the new menu instead of the player inventory: the client
 * keeps a ghost Codex on its cursor and stops seeing items it picks up until the inventory is
 * opened again. Letting the click finish first, then opening the menu, avoids both.
 */
final class CodexClickListener implements Listener {

    private final Plugin plugin;
    private final Codex codex;
    private final CodexMenus menus;

    CodexClickListener(Plugin plugin, Codex codex, CodexMenus menus) {
        this.plugin = plugin;
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
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                menus.main(player).open(player);
            }
        });
    }
}
