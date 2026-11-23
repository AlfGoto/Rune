package com.alfgoto.rune.foundations.triggers;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** A player broke the last natural log of a tree's trunk (see Felled Tree in CONTEXT.md). Fired once per tree. */
public final class TreeFelled extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Block lastLog;
    private final ItemStack item;
    private final List<Block> bases;

    public TreeFelled(Player player, Block lastLog, ItemStack item, List<Block> bases) {
        this.player = player;
        this.lastLog = lastLog;
        this.item = item;
        this.bases = List.copyOf(bases);
    }

    /**
     * Where the tree stood: the logs of its lowest layer (one for most trees, four for a 2x2 dark oak). The
     * last log is still in the world while the event runs; the others are gone.
     */
    public List<Block> bases() {
        return bases;
    }

    public Player player() {
        return player;
    }

    /** The log that was broken last. It is still in the world while the event runs. */
    public Block lastLog() {
        return lastLog;
    }

    /** The item held in the main hand (air when empty). */
    public ItemStack item() {
        return item;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
