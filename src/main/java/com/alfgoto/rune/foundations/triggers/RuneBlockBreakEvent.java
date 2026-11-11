package com.alfgoto.rune.foundations.triggers;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** A player broke a block. Never fired for TNT, creepers or other non-player breaks. */
public final class RuneBlockBreakEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Block block;
    private final BreakCause cause;
    private final ItemStack item;

    public RuneBlockBreakEvent(Player player, Block block, BreakCause cause, ItemStack item) {
        this.player = player;
        this.block = block;
        this.cause = cause;
        this.item = item;
    }

    public Player player() {
        return player;
    }

    public Block block() {
        return block;
    }

    public BreakCause cause() {
        return cause;
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
