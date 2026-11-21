package com.alfgoto.rune.visuals.feedback;

import com.alfgoto.rune.gameplay.quests.Quest;
import com.alfgoto.rune.gameplay.quests.QuestProgressEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Shows Quest progress in the action bar. */
public final class QuestFeedback implements Listener {

    @EventHandler
    public void onProgress(QuestProgressEvent event) {
        Quest quest = event.quest();
        event.player()
                .sendActionBar(Component.translatable(
                                "rune.quest.progress",
                                "%s: %s/%s",
                                Component.translatable(quest.type().translationKey(), quest.type().fallbackName()),
                                Component.text(event.done()),
                                Component.text(quest.target()))
                        .color(NamedTextColor.YELLOW));
    }
}
