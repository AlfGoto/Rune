package com.alfgoto.rune.foundations.triggers;

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

    public TreeFelled(Player player, Block lastLog, ItemStack item) {
        this.player = player;
        this.lastLog = lastLog;
        this.item = item;
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
