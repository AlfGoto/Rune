package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Spruce Axe: while it is in the main hand, the player does not freeze in powder snow and is not slowed
 * down by it. Freezing is cleared (no frost, no freeze damage). The slowdown is cancelled by a temporary
 * move speed bonus that is on only while the player stands in powder snow.
 */
final class PowderSnowListener {

    private final Plugin plugin;
    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final Predicate<ItemStack> isBroken;
    private final NamespacedKey key;

    PowderSnowListener(Plugin plugin, ItemRegistry items, ToolLoadout loadout, Predicate<ItemStack> isBroken) {
        this.plugin = plugin;
        this.items = items;
        this.loadout = loadout;
        this.isBroken = isBroken;
        this.key = new NamespacedKey(plugin, "powder_snow_immunity");
    }

    /** Starts the repeating check (every 5 ticks), so frost never shows, even when the player stands still. */
    void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 5L, 5L);
    }

    private void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (player.getFreezeTicks() > 0 && immune(player, tool)) {
                player.setFreezeTicks(0);
            }
            refresh(player, tool, player.getLocation());
        }
    }

    private boolean immune(Player player, ItemStack tool) {
        return tool != null
                && items.of(tool).isPresent()
                && !isBroken.test(tool)
                && loadout.total(tool, AxeTraits.POWDER_SNOW_IMMUNITY) > 0;
    }

    private void refresh(Player player, ItemStack tool, Location at) {
        AttributeInstance speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(key);
        if (at.getBlock().getType() == Material.POWDER_SNOW && immune(player, tool)) {
            speed.addTransientModifier(new AttributeModifier(
                    key, AxeTraits.powderSnowCompensation(), AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }
}
