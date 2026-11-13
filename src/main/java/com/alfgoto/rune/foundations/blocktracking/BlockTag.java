package com.alfgoto.rune.foundations.blocktracking;

/** A fact Block Tracking remembers about a block position. */
public enum BlockTag {
    /** The block was placed by a player. Gives no Job XP. */
    PLACED("placed"),
    /** The block was created by fluids (cobblestone, stone, basalt, obsidian). Obsidian keeps its Job XP anyway. */
    FLUID("fluid");

    private final String id;

    BlockTag(String id) {
        this.id = id;
    }

    /** Stable id, used in storage keys and in debug output. */
    public String id() {
        return id;
    }
}
