package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.gameplay.effects.Scale;
import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * One line of a Trait: an effect id, a value and a {@link Scale}. The value works like a Rune's value at
 * Rank 1 (no Rank). It can be negative: a drawback. Final value = value x scale.
 */
public record TraitBinding(String effectId, double value, Scale scale) {

    public TraitBinding {
        Objects.requireNonNull(effectId, "effectId");
        Objects.requireNonNull(scale, "scale");
        if (effectId.isBlank()) {
            throw new IllegalArgumentException("Effect id must not be blank");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Value must be a finite number: " + value);
        }
    }

    /** A binding that is always fully on. */
    public static TraitBinding always(String effectId, double value) {
        return new TraitBinding(effectId, value, Scale.ALWAYS);
    }

    /** This binding as an effect source for {@code player} and {@code stack}. */
    public Stacking.Source source(@Nullable Player player, @Nullable ItemStack stack) {
        double scaled = Math.max(0, Math.min(1, scale.at(player, stack)));
        return new Stacking.Source(effectId, value * scaled);
    }
}
