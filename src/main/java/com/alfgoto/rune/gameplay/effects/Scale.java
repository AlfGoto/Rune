package com.alfgoto.rune.gameplay.effects;

import com.alfgoto.rune.foundations.registry.Identified;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * How much of a Trait binding's value applies right now: a number from 0 to 1 for a player and a stack.
 * Final value = value x scale. Registered by id in the {@link ScaleRegistry}, like Effects.
 *
 * <p>{@code player} is null when the value is read with no player at hand; a Scale that needs a player
 * then gives 0.
 */
public interface Scale extends Identified {

    /** The Scale that is always 1. */
    Scale ALWAYS = new Scale() {
        @Override
        public String id() {
            return "always";
        }

        @Override
        public double at(@Nullable Player player, @Nullable ItemStack stack) {
            return 1;
        }
    };

    /** A number from 0 to 1. */
    double at(@Nullable Player player, @Nullable ItemStack stack);
}
