package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

/**
 * Applies the Rhythm armor Traits of the worn pieces (their values already follow the charges):
 * log breaking speed (a modifier set when a log starts breaking), damage and fall damage reduction (on damage),
 * and movement speed and jump strength (modifiers refreshed when charges or armor change).
 */
final class RhythmArmorListener implements Listener {

    private final Plugin plugin;
    private final ToolLoadout loadout;
    private final NamespacedKey chopKey;
    private final NamespacedKey moveKey;
    private final NamespacedKey jumpKey;

    RhythmArmorListener(Plugin plugin, ToolLoadout loadout) {
        this.plugin = plugin;
        this.loadout = loadout;
        this.chopKey = new NamespacedKey(plugin, "rhythm_chop_speed");
        this.moveKey = new NamespacedKey(plugin, "rhythm_move_speed");
        this.jumpKey = new NamespacedKey(plugin, "rhythm_jump");
    }

    @EventHandler
    public void onStartBreaking(BlockDamageEvent event) {
        Player player = event.getPlayer();
        AttributeInstance speed = player.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(chopKey);
        if (!Tag.LOGS.isTagged(event.getBlock().getType())) {
            return;
        }
        apply(speed, chopKey, loadout.wornTotal(player, RhythmRules.CHOP_SPEED));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        double factor = 1 - fraction(loadout.wornTotal(player, RhythmRules.DAMAGE_REDUCTION));
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            factor *= 1 - fraction(loadout.wornTotal(player, RhythmRules.FALL_REDUCTION));
        }
        if (factor < 1) {
            event.setDamage(event.getDamage() * factor);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRhythmChange(RhythmChangeEvent event) {
        refresh(event.player());
    }

    // Armor changes through the inventory (click, drag) or a right-click equip.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            later(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            later(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            later(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemBreak(PlayerItemBreakEvent event) {
        later(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        later(event.getPlayer());
    }

    // Refresh one tick later, once the armor or the player is in its new state.
    private void later(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                refresh(player);
            }
        });
    }

    /** Sets the movement speed and jump strength modifiers from the worn pieces and the charges now. */
    void refresh(Player player) {
        AttributeInstance move = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (move != null) {
            move.removeModifier(moveKey);
            apply(move, moveKey, loadout.wornTotal(player, RhythmRules.MOVE_SPEED));
        }
        AttributeInstance jump = player.getAttribute(Attribute.JUMP_STRENGTH);
        if (jump != null) {
            jump.removeModifier(jumpKey);
            apply(jump, jumpKey, loadout.wornTotal(player, RhythmRules.JUMP));
        }
    }

    private static void apply(AttributeInstance attribute, NamespacedKey key, double percent) {
        if (percent > 0) {
            attribute.addTransientModifier(
                    new AttributeModifier(key, percent / 100, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }

    /** A percent as a number from 0 to 1. */
    private static double fraction(double percent) {
        return Math.max(0, Math.min(1, percent / 100));
    }
}
