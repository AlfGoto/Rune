package com.alfgoto.rune.gameplay.quests;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Fired when a Quest gets progress but is not finished yet. Feedback shows it; Gameplay shows nothing. */
public final class QuestProgressEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Quest quest;
    private final int done;

    public QuestProgressEvent(Player player, Quest quest, int done) {
        this.player = player;
        this.quest = quest;
        this.done = done;
    }

    public Player player() {
        return player;
    }

    public Quest quest() {
        return quest;
    }

    /** How much of the objective is done. */
    public int done() {
        return done;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
