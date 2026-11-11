package com.alfgoto.rune.foundations.triggers;

/** How a player broke a block (see Job Action in CONTEXT.md). */
public enum BreakCause {
    /** Broken with a tool in hand. */
    TOOL,
    /** Broken by a Rune effect acting for the player (see {@link Triggers#asEffect(Runnable)}). */
    EFFECT,
    /** Broken with an empty hand. */
    HAND
}
