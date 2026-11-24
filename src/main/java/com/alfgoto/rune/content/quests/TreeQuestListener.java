package com.alfgoto.rune.content.quests;

import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.SaplingPlanters;
import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.quests.QuestAction;
import com.alfgoto.rune.gameplay.quests.QuestActionEvent;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.jspecify.annotations.Nullable;

/**
 * Turns Felled Trees and trees grown from saplings into {@link QuestActionEvent}s for the Woodcutting Quests.
 *
 * <p>Only successful growths count (one per tree, never per sapling planted). Bone meal credits the player who
 * used it. Natural growth credits the player who planted the sapling, if they are online. A dispenser has no
 * planter and no player: its trees count for no one.
 */
final class TreeQuestListener implements Listener {

    /** Growths that fire {@link StructureGrowEvent} but are not trees. */
    private static final Set<TreeType> NOT_TREES = EnumSet.of(
            TreeType.RED_MUSHROOM,
            TreeType.BROWN_MUSHROOM,
            TreeType.CHORUS_PLANT,
            TreeType.CRIMSON_FUNGUS,
            TreeType.WARPED_FUNGUS);

    /** Species grown from a 2x2 of saplings (by name, so a missing constant on older APIs cannot break us). */
    private static final Set<String> BIG_TREES =
            Set.of("JUNGLE", "DARK_OAK", "MEGA_REDWOOD", "PALE_OAK", "PALE_OAK_CREAKING");

    private static boolean isBigTree(StructureGrowEvent event) {
        return BIG_TREES.contains(event.getSpecies().name());
    }

    private final Server server;
    private final SaplingPlanters planters;
    private final BlockTracker tracker;

    TreeQuestListener(Server server, SaplingPlanters planters, BlockTracker tracker) {
        this.server = server;
        this.tracker = tracker;
        this.planters = planters;
    }

    @EventHandler
    public void onTreeFelled(TreeFelled event) {
        // A bone-meal tree is not a natural tree: no bone-meal farm for the Tool Quest (same rule as Job XP).
        if (tracker.has(event.lastLog(), BlockTag.GROWN_BONEMEAL)) {
            return;
        }
        server.getPluginManager()
                .callEvent(new QuestActionEvent(
                        event.player(), CoreJobs.WOODCUTTING, QuestAction.TREE_FELLED, event.lastLog(), event.item()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (NOT_TREES.contains(event.getSpecies()) || event.getBlocks().isEmpty()) {
            return;
        }
        // Vanilla may already have removed the sapling: never read its block type here.
        Block sapling = event.getLocation().getBlock();
        Player credited = event.isFromBonemeal() ? event.getPlayer() : planterOf(sapling, isBigTree(event));
        planters.clear(sapling);
        if (credited != null) {
            server.getPluginManager()
                    .callEvent(new QuestActionEvent(
                            credited, CoreJobs.WOODCUTTING, QuestAction.TREE_GROWN, sapling, null));
        }
    }

    /**
     * The online player who planted this sapling. A 2x2 tree grows from one of 4 saplings, so for those species
     * (only) the neighbours are checked too. A dispensed sapling of a 1x1 tree never takes a neighbour's credit.
     */
    private @Nullable Player planterOf(Block sapling, boolean bigTree) {
        Optional<UUID> own = planters.get(sapling);
        if (own.isPresent()) {
            planters.clear(sapling);
            return server.getPlayer(own.get());
        }
        if (!bigTree) {
            return null;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block near = sapling.getRelative(dx, 0, dz);
                Optional<UUID> planter = planters.get(near);
                if (planter.isPresent()) {
                    planters.clear(near);
                    return server.getPlayer(planter.get());
                }
            }
        }
        return null;
    }
}
