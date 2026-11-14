package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.registry.Registry;

/** The shared Quest Type registry. Starts empty: content modules fill it. */
public final class QuestTypeRegistry {

    private final Registry<QuestType> types = new Registry<>("quest types");

    /** Adds a Quest Type. Throws if its id is already used. */
    public QuestType register(QuestType type) {
        return types.register(type);
    }

    /** The underlying registry. Read it when you need it: content modules add types after enable. */
    public Registry<QuestType> registry() {
        return types;
    }
}
