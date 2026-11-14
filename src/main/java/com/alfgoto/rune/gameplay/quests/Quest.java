package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.text.Rarity;

/** An active Quest: a Quest Type drawn at a Quest Tier, with its objective size (base size × Tier multiplier). */
public record Quest(QuestType type, Rarity tier, int target) {}
