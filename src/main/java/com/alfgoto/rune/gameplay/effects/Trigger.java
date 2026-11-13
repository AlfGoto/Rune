package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.registry.Identified;
import java.util.Objects;
import java.util.function.Function;
import org.bukkit.entity.Player;

/**
 * What starts an Effect: one kind of Rune event (block break, click in air…) and how to find the player
 * it concerns.
 */
public final class Trigger<E> implements Identified {

    private final String id;
    private final Class<E> eventType;
    private final Function<E, Player> player;

    public Trigger(String id, Class<E> eventType, Function<E, Player> player) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Trigger id must not be blank");
        }
        this.id = id;
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.player = Objects.requireNonNull(player, "player");
    }

    @Override
    public String id() {
        return id;
    }

    public Class<E> eventType() {
        return eventType;
    }

    /** The player the event is about. */
    public Player playerOf(E event) {
        return player.apply(event);
    }

    @Override
    public String toString() {
        return "Trigger[" + id + "]";
    }
}
