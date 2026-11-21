package com.alfgoto.rune.gameplay.jobs;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after a player performs a Job Action that gave Job XP (e.g. a natural block broken).
 * Unlike {@link JobXpListener}, it is not fired for XP from rewards, so Quests count only real actions.
 */
public final class JobActionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Job job;
    private final double xp;
    private final Location location;

    public JobActionEvent(Player player, Job job) {
        this(player, job, 0, null);
    }

    /** {@code xp} is the Job XP the action gave; {@code location} is where it happened (the block), or null. */
    public JobActionEvent(Player player, Job job, double xp, Location location) {
        this.player = player;
        this.job = job;
        this.xp = xp;
        this.location = location;
    }

    /** Job XP the action gave (0 when unknown). */
    public double xp() {
        return xp;
    }

    /** Where the action happened (the block's center), or null when unknown. */
    public Location location() {
        return location;
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
