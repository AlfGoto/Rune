package com.alfgoto.rune.gameplay.quests;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Fills the Quest slots when a player joins, so they always have 3 active Quests. */
public final class QuestJoinListener implements Listener {

    private final QuestBook book;

    public QuestJoinListener(QuestBook book) {
        this.book = book;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        book.active(event.getPlayer().getUniqueId());
    }
}
