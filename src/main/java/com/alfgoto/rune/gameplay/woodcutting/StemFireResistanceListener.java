package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.function.Predicate;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Crimson Axe: breaking a Crimson or Warped stem (or hyphae, stripped or not) with the axe gives a short
 * Fire Resistance. A longer or stronger Fire Resistance the player already has is kept.
 */
final class StemFireResistanceListener implements Listener {

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final Predicate<ItemStack> isBroken;

    StemFireResistanceListener(ItemRegistry items, ToolLoadout loadout, Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.isBroken = isBroken;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(RuneBlockBreakEvent event) {
        var type = event.block().getType();
        if (!Tag.CRIMSON_STEMS.isTagged(type) && !Tag.WARPED_STEMS.isTagged(type)) {
            return;
        }
        ItemStack tool = event.item();
        if (tool == null || items.of(tool).isEmpty() || isBroken.test(tool)) {
            return;
        }
        Player player = event.player();
        double seconds = Stacking.total(AxeTraits.STEM_FIRE_RESISTANCE, loadout.sources(tool, player));
        if (seconds <= 0) {
            return;
        }
        AxeTraits.giveKeepingStronger(player, PotionEffectType.FIRE_RESISTANCE, seconds);
    }
}
