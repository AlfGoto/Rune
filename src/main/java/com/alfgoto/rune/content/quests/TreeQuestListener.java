package com.alfgoto.rune.content.quests;

import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.foundations.blocktracking.SaplingPlanters;
import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.quests.QuestAction;
import com.alfgoto.rune.gameplay.quests.QuestActionEvent;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.Tag;
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

    private final Server server;
    private final SaplingPlanters planters;

    TreeQuestListener(Server server, SaplingPlanters planters) {
        this.server = server;
        this.planters = planters;
    }

    @EventHandler
    public void onTreeFelled(TreeFelled event) {
        server.getPluginManager()
                .callEvent(new QuestActionEvent(
                        event.player(), CoreJobs.WOODCUTTING, QuestAction.TREE_FELLED, event.lastLog(), event.item()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        Block sapling = event.getLocation().getBlock();
        if (!Tag.SAPLINGS.isTagged(sapling.getType()) || event.getBlocks().isEmpty()) {
            return;
        }
        Player credited = event.isFromBonemeal() ? event.getPlayer() : planterOf(sapling);
        planters.clear(sapling);
        if (credited != null) {
            server.getPluginManager()
                    .callEvent(new QuestActionEvent(
                            credited, CoreJobs.WOODCUTTING, QuestAction.TREE_GROWN, sapling, null));
        }
    }

    /**
     * The online player who planted this sapling. A 2x2 tree grows from one of 4 saplings, so same-type saplings
     * next to it are checked too.
     */
    private @Nullable Player planterOf(Block sapling) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block near = sapling.getRelative(dx, 0, dz);
                if (near.getType() != sapling.getType()) {
                    continue;
                }
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
