package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;

/** The Triggers that exist today, one per Rune event. */
public final class EffectTriggers {

    public static final Trigger<RuneBlockBreakEvent> BLOCK_BREAK =
            new Trigger<>("block_break", RuneBlockBreakEvent.class, RuneBlockBreakEvent::player);

    public static final Trigger<RuneClickEvent> CLICK =
            new Trigger<>("click", RuneClickEvent.class, RuneClickEvent::player);

    private EffectTriggers() {}
}
