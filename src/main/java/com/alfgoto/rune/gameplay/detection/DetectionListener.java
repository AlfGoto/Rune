package com.alfgoto.rune.gameplay.detection;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The {@code echo_ping} effect of a Rune Tool in hand: it pings by itself (see {@link #ping}) and only fires an
 * {@link OreRevealEvent}. The Ore Sense chance (Bone Pickaxe and the Rune) is in
 * {@code visuals.reveal.OreSenseListener}, so it rolls once per break.
 */
public final class DetectionListener {

    public static final String ECHO_PING = "echo_ping";

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final DetectionSettings settings;
    private final Predicate<ItemStack> isBroken;

    public DetectionListener(
            ItemRegistry items, ToolLoadout loadout, DetectionSettings settings, Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.settings = settings;
        this.isBroken = isBroken;
    }

    /** Pings around {@code player} if the tool in the main hand has the {@code echo_ping} effect. */
    public void ping(Player player) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (items.of(tool).isEmpty()
                || isBroken.test(tool)
                || Stacking.total(ECHO_PING, loadout.sources(tool, player)) <= 0) {
            return;
        }
        fire(player, OreScan.find(player.getLocation().getBlock(), settings.echoRadius()), settings.echoTicks());
    }

    private static void fire(Player player, List<Block> ores, int ticks) {
        if (!ores.isEmpty()) {
            player.getServer().getPluginManager().callEvent(new OreRevealEvent(player, ores, ticks));
        }
    }
}
