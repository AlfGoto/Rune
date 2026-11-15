package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.gameplay.effects.EffectBinding;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.RankValues;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Rarity;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * A Rune: an Effect (with its value per Rank and optional Max Rank) on a Rune item, with the places it
 * fits. Its Rarity is the one of the item. The Rank is not here: it lives on each item stack.
 */
public final class Rune implements Identified {

    private final RuneItem item;
    private final EffectBinding<?> effect;
    private final Set<Fit> fits;
    private final @Nullable Job job;
    private final @Nullable RankValues cooldown;

    /** A general Rune (no Job). */
    public Rune(RuneItem item, EffectBinding<?> effect, Set<Fit> fits) {
        this(item, effect, fits, null);
    }

    /** A Rune of {@code job}: a Rune Cache dropped by that Job leans towards it. {@code null} means a general Rune. */
    public Rune(RuneItem item, EffectBinding<?> effect, Set<Fit> fits, @Nullable Job job) {
        this(item, effect, fits, job, null);
    }

    /**
     * An Active Rune: its Effect runs on a click in air from an Active Slot, then the Rune is on cooldown for
     * {@code cooldownSeconds} (base + growth per Rank, in seconds).
     */
    public static Rune active(
            RuneItem item,
            EffectBinding<RuneClickEvent> effect,
            Set<Fit> fits,
            @Nullable Job job,
            RankValues cooldownSeconds) {
        return new Rune(item, effect, fits, job, Objects.requireNonNull(cooldownSeconds, "cooldownSeconds"));
    }

    private Rune(
            RuneItem item,
            EffectBinding<?> effect,
            Set<Fit> fits,
            @Nullable Job job,
            @Nullable RankValues cooldown) {
        this.job = job;
        this.cooldown = cooldown;
        this.item = Objects.requireNonNull(item, "item");
        this.effect = Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(fits, "fits");
        if (fits.isEmpty()) {
            throw new IllegalArgumentException("A Rune must fit somewhere: " + item.id());
        }
        this.fits = Set.copyOf(fits);
        if ((cooldown != null) != (effect.effect().trigger() == EffectTriggers.CLICK)) {
            throw new IllegalArgumentException(
                    "A Rune with a click Trigger must be built with Rune.active, and only those: " + item.id());
        }
    }

    @Override
    public String id() {
        return item.id();
    }

    public RuneItem item() {
        return item;
    }

    /** True for an Active Rune: it runs on a click in air from an Active Slot and has a cooldown. */
    public boolean isActive() {
        return cooldown != null;
    }

    /** The cooldown in seconds at {@code rank}. Throws if this is not an Active Rune. */
    public double cooldownSeconds(int rank) {
        if (cooldown == null) {
            throw new IllegalStateException("Not an Active Rune: " + id());
        }
        return cooldown.at(rank);
    }

    /** The Job this Rune belongs to, or empty for a general Rune. */
    public Optional<Job> job() {
        return Optional.ofNullable(job);
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
