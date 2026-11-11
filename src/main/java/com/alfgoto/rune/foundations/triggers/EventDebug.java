package com.alfgoto.rune.foundations.triggers;

import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Logs every Rune event to the console while enabled ({@code /rune debug events}). */
public final class EventDebug implements Listener {

    private final Logger logger;
    private volatile boolean enabled;

    public EventDebug(Logger logger) {
        this.logger = logger;
    }

    public boolean enabled() {
        return enabled;
    }

    /** Flips the toggle and returns the new state. */
    public synchronized boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    @EventHandler
    public void onBreak(RuneBlockBreakEvent event) {
        if (enabled) {
            logger.info("[events] %s broke %s (%s) with %s"
                    .formatted(
                            event.player().getName(),
                            event.block().getType(),
                            event.cause(),
                            event.item().getType()));
        }
    }

    @EventHandler
    public void onClick(RuneClickEvent event) {
        if (enabled) {
            logger.info("[events] %s %s-clicked in air with %s"
                    .formatted(event.player().getName(), event.click(), event.item().getType()));
        }
    }
}
