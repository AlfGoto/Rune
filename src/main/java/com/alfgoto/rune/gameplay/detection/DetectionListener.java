package com.alfgoto.rune.gameplay.detection;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Detection effects of a Rune Tool in hand:
 * <ul>
 *   <li>{@code ore_sense}: a chance (in percent, stacked) that breaking a stone-family block outlines the ores
 *       nearby. Placed blocks do not count.</li>
 *   <li>{@code echo_ping}: the tool pings by itself (see {@link #ping}).</li>
 * </ul>
 * Both only fire an {@link OreRevealEvent}.
 */
public final class DetectionListener implements Listener {

    public static final String ORE_SENSE = "ore_sense";
    public static final String ECHO_PING = "echo_ping";

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlockTracker tracker;
    private final DetectionSettings settings;
    private final DoubleSupplier random;
    private final Predicate<ItemStack> isBroken;

    public DetectionListener(
            ItemRegistry items,
            ToolLoadout loadout,
            BlockTracker tracker,
            DetectionSettings settings,
            DoubleSupplier random,
            Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.tracker = tracker;
        this.settings = settings;
        this.random = random;
        this.isBroken = isBroken;
    }

    @EventHandler
    public void onBreak(RuneBlockBreakEvent event) {
        if (event.cause() != BreakCause.TOOL
                || !OreScan.isStoneFamily(event.block().getType())
                || tracker.has(event.block(), BlockTag.PLACED)
                || items.of(event.item()).isEmpty()) {
            return;
        }
        double chance = Stacking.total(ORE_SENSE, loadout.sources(event.item(), event.player()));
        if (Stacking.rolls(chance, random) == 0) {
            return;
        }
        fire(event.player(), OreScan.find(event.block(), settings.oreSenseRadius()), settings.oreSenseTicks());
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
