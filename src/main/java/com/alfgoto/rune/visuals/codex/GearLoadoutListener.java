package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.visuals.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

/**
 * Right-clicking a Rune Tool or Rune Armor piece in the player's own inventory (hotbar, main inventory, worn
 * armor, offhand) with an empty cursor opens its Loadout ({@link GearMenu}), one tick later: the click is
 * cancelled and the menu opens after it, like {@link CodexClickListener} (opening inside the click breaks the
 * client). Left-click, shift-click, number keys and drops stay vanilla, and so does a right-click with an item on
 * the cursor (kept for a later "drop a Rune onto the gear" shortcut). Creative mode is not supported.
 */
final class GearLoadoutListener implements Listener {

    private final Plugin plugin;
    private final ToolLoadout loadout;
    private final ItemRegistry items;
    private final RuneRegistry runes;

    GearLoadoutListener(Plugin plugin, ToolLoadout loadout, ItemRegistry items, RuneRegistry runes) {
        this.plugin = plugin;
        this.loadout = loadout;
        this.items = items;
        this.runes = runes;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        if (!(event.getWhoClicked() instanceof Player player)
                || !(event.getClickedInventory() instanceof PlayerInventory)
                || event.getClick() != ClickType.RIGHT
                || !(cursor == null || cursor.getType().isAir())
                || !loadout.isGear(event.getCurrentItem())
                || event.getView().getTopInventory().getHolder() instanceof Menu) {
            return;
        }
        int slot = event.getSlot();
        event.setCancelled(true);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                new GearMenu(player, runes, loadout, items, slot).open(player);
            }
        });
    }
}
