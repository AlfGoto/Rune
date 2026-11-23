package com.alfgoto.rune.visuals.reveal;

import com.alfgoto.rune.gameplay.detection.OreRevealEvent;
import org.bukkit.Color;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Outlines the ores of an {@link OreRevealEvent} for the player (Ore Sense, Bone, Echo). */
final class OreRevealListener implements Listener {

    private final BlockReveal reveal;

    OreRevealListener(BlockReveal reveal) {
        this.reveal = reveal;
    }

    @EventHandler
    public void onReveal(OreRevealEvent event) {
        reveal.reveal(event.player(), event.ores(), event.ticks(), Color.AQUA);
    }
}
