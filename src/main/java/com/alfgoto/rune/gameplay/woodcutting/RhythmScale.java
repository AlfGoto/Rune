package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.effects.Scale;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** The {@code rhythm} Scale: the player's charges divided by the most charges (0 with no player). */
public final class RhythmScale implements Scale {

    public static final String ID = "rhythm";

    private final Rhythm rhythm;

    public RhythmScale(Rhythm rhythm) {
        this.rhythm = rhythm;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public double at(@Nullable Player player, @Nullable ItemStack stack) {
        return player == null ? 0 : rhythm.fraction(player.getUniqueId());
    }
}
