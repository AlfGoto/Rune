package com.alfgoto.rune.gameplay.jobs;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Fired after a player performs a Job Action that gave Job XP (e.g. a natural block broken).
 * Unlike {@link JobXpListener}, it is not fired for XP from rewards, so Quests count only real actions.
 */
public final class JobActionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Job job;
    private final double xp;
    private final @Nullable Block block;
    private final @Nullable ItemStack tool;

    public JobActionEvent(Player player, Job job) {
        this(player, job, 0, null, null);
    }

    /** A block action with no XP info (xp = 0). */
    public JobActionEvent(Player player, Job job, @Nullable Block block, @Nullable ItemStack tool) {
        this(player, job, 0, block, tool);
    }

    /**
     * A block action: {@code xp} is the Job XP it gave, {@code block} the broken block,
     * {@code tool} the item held in the main hand.
     */
    public JobActionEvent(Player player, Job job, double xp, @Nullable Block block, @Nullable ItemStack tool) {
        this.player = player;
        this.job = job;
        this.xp = xp;
        this.block = block;
        this.tool = tool;
    }

    /** Job XP the action gave (0 when unknown). */
    public double xp() {
        return xp;
    }

    /** Where the action happened (the block's center), or null when it is not a block break. */
    public @Nullable Location location() {
        return block == null ? null : block.getLocation().add(0.5, 0.5, 0.5);
    }

    /** The block that was broken, or null when the action is not a block break. */
    public @Nullable Block block() {
        return block;
    }

    /** The item held in the main hand when the action happened, or null when unknown. */
    public @Nullable ItemStack tool() {
        return tool;
    }

    public Player player() {
        return player;
    }

    public Job job() {
        return job;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
