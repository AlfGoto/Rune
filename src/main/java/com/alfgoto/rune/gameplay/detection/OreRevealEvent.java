package com.alfgoto.rune.gameplay.detection;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Detection found ores for a player (Ore Sense, Bone, Echo): they should be outlined for {@code ticks}.
 * Gameplay only fires it; the Visuals family shows the outline (nothing in Gameplay depends on Visuals).
 */
public final class OreRevealEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final List<Block> ores;
    private final int ticks;

    public OreRevealEvent(Player player, List<Block> ores, int ticks) {
        this.player = player;
        this.ores = List.copyOf(ores);
        this.ticks = ticks;
    }

    public Player player() {
        return player;
    }

    public List<Block> ores() {
        return ores;
    }

    /** How long the outline stays, in ticks. */
    public int ticks() {
        return ticks;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
