package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.quests.QuestProgressEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Keeps an open Quests menu live: redraws it when its owner's Quest progresses. */
final class QuestsMenuRefresher implements Listener {

    @EventHandler
    public void onProgress(QuestProgressEvent event) {
        var top = event.player().getOpenInventory().getTopInventory();
        if (top != null
                && top.getHolder() instanceof QuestsMenu menu
                && menu.owner().equals(event.player().getUniqueId())) {
            menu.refresh();
        }
    }
}
