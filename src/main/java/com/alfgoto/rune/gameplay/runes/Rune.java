package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.gameplay.effects.EffectBinding;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Rarity;
import java.util.EnumSet;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;

/**
 * A Rune: an Effect (with its value per Rank and optional Max Rank) on a Rune item, with the places it
 * fits. Its Rarity is the one of the item. The Rank is not here: it lives on each item stack.
 */
public final class Rune implements Identified {

    private final RuneItem item;
    private final EffectBinding<?> effect;
    private final Set<Fit> fits;

    public Rune(RuneItem item, EffectBinding<?> effect, Set<Fit> fits) {
        this.item = Objects.requireNonNull(item, "item");
        this.effect = Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(fits, "fits");
        if (fits.isEmpty()) {
            throw new IllegalArgumentException("A Rune must fit somewhere: " + item.id());
        }
        this.fits = Set.copyOf(fits);
    }

    @Override
    public String id() {
        return item.id();
    }

    public RuneItem item() {
        return item;
    }

    public Rarity rarity() {
        return item.rarity();
    }

    public EffectBinding<?> effect() {
        return effect;
    }

    /** The places this Rune fits, in {@link Fit} order. */
    public Set<Fit> fits() {
        return EnumSet.copyOf(fits);
    }

    /** The Max Rank, or empty if this Rune has none. */
    public OptionalInt maxRank() {
        return effect.values().maxRank();
    }

    /** True if {@code rank} is a valid Rank for this Rune. Check it when ranking up or giving. */
    public boolean allowsRank(int rank) {
        return effect.values().allows(rank);
    }
}
