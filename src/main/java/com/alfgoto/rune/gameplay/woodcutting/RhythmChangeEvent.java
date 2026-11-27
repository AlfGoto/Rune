package com.alfgoto.rune.gameplay.woodcutting;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A player's Rhythm charges changed: one more after a Felled Tree, or all lost after the reset time.
 * The flag tells if the player wears a piece with a Rhythm Trait (only then is it shown).
 */
public final class RhythmChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int charges;
    private final int maxCharges;
    private final boolean wearingRhythmGear;

    public RhythmChangeEvent(Player player, int charges, int maxCharges, boolean wearingRhythmGear) {
        this.player = player;
        this.charges = charges;
        this.maxCharges = maxCharges;
        this.wearingRhythmGear = wearingRhythmGear;
    }

    public Player player() {
        return player;
    }

    /** The charges after the change (0 = all lost). */
    public int charges() {
        return charges;
    }

    public int maxCharges() {
        return maxCharges;
    }

    public boolean wearingRhythmGear() {
        return wearingRhythmGear;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
