package com.alfgoto.rune.gameplay.effects;

import org.bukkit.entity.Player;

/** What a Condition or an action sees: the event, its player, the Rank and the value for that Rank. */
public record EffectContext<E>(E event, Player player, int rank, double value) {}
