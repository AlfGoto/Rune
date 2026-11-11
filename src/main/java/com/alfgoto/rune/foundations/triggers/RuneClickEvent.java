package com.alfgoto.rune.foundations.triggers;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/** A player clicked in the air with the item in their main hand. */
public final class RuneClickEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ClickType click;
    private final ItemStack item;

    public RuneClickEvent(Player player, ClickType click, ItemStack item) {
        this.player = player;
        this.click = click;
        this.item = item;
    }

    public Player player() {
        return player;
    }

    public ClickType click() {
        return click;
    }

    /** The held item (air when empty). */
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
