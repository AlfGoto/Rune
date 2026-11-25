package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.gameplay.jobs.Job;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A Quest-only action that is not a block break: a Felled Tree or a tree grown from a sapling. It gives no Job
 * XP and no Cache: only {@link QuestTracker} listens to it.
 */
public final class QuestActionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Job job;
    private final QuestAction action;
    private final @Nullable Block block;
    private final @Nullable ItemStack tool;

    public QuestActionEvent(
            Player player, Job job, QuestAction action, @Nullable Block block, @Nullable ItemStack tool) {
        this.player = player;
        this.job = job;
        this.action = action;
        this.block = block;
        this.tool = tool;
    }

    public Player player() {
        return player;
    }

    public Job job() {
        return job;
    }

    public QuestAction action() {
        return action;
    }

    /** The last log (felled) or the sapling's block (grown). */
    public @Nullable Block block() {
        return block;
    }

    /** The item held in the main hand, when it matters (felled trees). */
    public @Nullable ItemStack tool() {
        return tool;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
