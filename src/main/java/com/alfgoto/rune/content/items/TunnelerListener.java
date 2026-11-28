package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.triggers.BreakCause;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.gameplay.veins.TunnelScan;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * The Tunneler Pickaxe Trait (docs/jobs/mining.md): mining a block with a tool that has the {@code tunneler}
 * effect also mines the 3x3 square around it, facing the side the player mined. Only blocks a pickaxe mines are
 * taken (never bedrock). The 8 extra blocks go through {@link MultiBreak}, so each gives full Job XP, a Cache
 * roll and wears the tool.
 */
final class TunnelerListener implements Listener {

    static final String EFFECT_ID = "tunneler";

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final MultiBreak multiBreak;
    private final Predicate<ItemStack> isBroken;
    /** The face each player last started to mine: the break event does not tell it. */
    private final Map<UUID, BlockFace> faces = new HashMap<>();

    TunnelerListener(ItemRegistry items, ToolLoadout loadout, MultiBreak multiBreak, Predicate<ItemStack> isBroken) {
        this.items = items;
        this.loadout = loadout;
        this.multiBreak = multiBreak;
        this.isBroken = isBroken;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.useInteractedBlock() != Event.Result.DENY) {
            faces.put(event.getPlayer().getUniqueId(), event.getBlockFace());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        faces.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(RuneBlockBreakEvent event) {
        // Only the player's own break starts a tunnel: blocks broken by an effect never start another one.
        if (event.cause() != BreakCause.TOOL || items.of(event.item()).isEmpty()) {
            return;
        }
        BlockFace face = faces.remove(event.player().getUniqueId());
        if (Stacking.total(EFFECT_ID, loadout.sources(event.item(), event.player())) <= 0) {
            return;
        }
        if (face == null) {
            face = lookFace(event.player());
        }
        List<Block> square = TunnelScan.square(event.block(), face, TunnelerListener::isMineable);
        multiBreak.breakAll(event.player(), square, tool -> items.of(tool).isPresent() && !isBroken.test(tool));
    }

    /** A block a pickaxe mines: no air, fluids or unbreakable blocks (bedrock...). */
    private static boolean isMineable(Block block) {
        return Tag.MINEABLE_PICKAXE.isTagged(block.getType()) && block.getType().getHardness() >= 0;
    }

    /** The side the player faces, when the click was not seen: the strongest axis of where they look. */
    private static BlockFace lookFace(Player player) {
        Vector look = player.getEyeLocation().getDirection();
        double x = Math.abs(look.getX());
        double y = Math.abs(look.getY());
        double z = Math.abs(look.getZ());
        if (y >= x && y >= z) {
            return look.getY() > 0 ? BlockFace.DOWN : BlockFace.UP;
        }
        if (x >= z) {
            return look.getX() > 0 ? BlockFace.WEST : BlockFace.EAST;
        }
        return look.getZ() > 0 ? BlockFace.NORTH : BlockFace.SOUTH;
    }
}
