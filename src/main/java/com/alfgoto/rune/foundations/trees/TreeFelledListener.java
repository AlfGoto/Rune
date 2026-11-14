package com.alfgoto.rune.foundations.trees;

import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.TreeFelled;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Fires {@link TreeFelled} when a player breaks the last natural log of a trunk. Only the player's own breaks
 * count, whatever the cause (tool, hand or effect): a Treefeller fells its tree when its last log breaks.
 *
 * <p>The broken block is still in the world while this runs and keeps its tags, so Block Tracking's clear on break
 * does not matter here.
 */
public final class TreeFelledListener implements Listener {

    private final Server server;
    private final TreeScan scan;

    public TreeFelledListener(Server server, TreeScan scan) {
        this.server = server;
        this.scan = scan;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(RuneBlockBreakEvent event) {
        if (scan.isLastLog(event.block())) {
            server.getPluginManager().callEvent(new TreeFelled(event.player(), event.block(), event.item()));
        }
    }
}
