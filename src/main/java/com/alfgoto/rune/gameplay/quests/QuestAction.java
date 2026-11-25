package com.alfgoto.rune.gameplay.quests;

/** The kind of action a Quest Type counts. */
public enum QuestAction {
    /** A natural block broken for Job XP (a {@code JobActionEvent}). The default. */
    BLOCK,
    /** A Felled Tree (see CONTEXT.md). */
    TREE_FELLED,
    /** A tree that grew from a sapling. */
    TREE_GROWN
}
