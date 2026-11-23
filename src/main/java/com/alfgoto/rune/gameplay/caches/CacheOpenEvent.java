package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player opens a Rune Cache, before the loot is given. The loot is given {@link #revealTicks()}
 * later (at once when 0), so visuals can reveal it first (the Cache reel). Visuals read it, never change it.
 */
public final class CacheOpenEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Rarity rarity;
    private final List<Drop> drops;
    private final int revealTicks;

    public CacheOpenEvent(Player player, Rarity rarity, List<Drop> drops, int revealTicks) {
        this.player = player;
        this.rarity = rarity;
        this.drops = List.copyOf(drops);
        this.revealTicks = revealTicks;
    }

    public Player player() {
        return player;
    }

    /** The Rarity of the opened Cache. */
    public Rarity rarity() {
        return rarity;
    }

    /** What the Cache gives, in roll order. */
    public List<Drop> drops() {
        return drops;
    }

    /** Ticks until the loot is given. */
    public int revealTicks() {
        return revealTicks;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
