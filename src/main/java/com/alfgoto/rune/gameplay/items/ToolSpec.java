package com.alfgoto.rune.gameplay.items;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * What makes a {@link RuneItem} a Rune Tool: the overrides of its stats. A {@code null} override
 * means "default from Rarity / Harvest Tier" (see {@link ToolStats}).
 *
 * @param tierOffset Harvest Tier shift from the Rarity default: -1, 0 or +1
 * @param speed mining speed override
 * @param durability durability override
 * @param slots Rune slots override
 * @param capacity Capacity override
 * @param trait id of the tool's Trait (shown in the lore), or {@code null} for none
 * @param traitBindings the Trait bindings: what the Trait does (empty for none)
 * @param capacityModifier added to the Capacity (negative = drawback)
 */
public record ToolSpec(
        int tierOffset,
        @Nullable Float speed,
        @Nullable Integer durability,
        @Nullable Integer slots,
        @Nullable Integer capacity,
        @Nullable String trait,
        List<TraitBinding> traitBindings,
        int capacityModifier) {

    /** A plain tool: every stat from its Rarity. */
    public static final ToolSpec DEFAULT = new ToolSpec(0, null, null, null, null, null);

    /** A tool with a Trait id only (no bindings, no Capacity change). */
    public ToolSpec(
            int tierOffset,
            @Nullable Float speed,
            @Nullable Integer durability,
            @Nullable Integer slots,
            @Nullable Integer capacity,
            @Nullable String trait) {
        this(tierOffset, speed, durability, slots, capacity, trait, List.of(), 0);
    }

    public ToolSpec {
        traitBindings = List.copyOf(traitBindings);
        if (tierOffset < -1 || tierOffset > 1) {
            throw new IllegalArgumentException("Harvest Tier offset must be -1, 0 or 1: " + tierOffset);
        }
        if (speed != null && !(speed > 0)) {
            throw new IllegalArgumentException("Speed must be greater than 0: " + speed);
        }
        if (durability != null && durability < 1) {
            throw new IllegalArgumentException("Durability must be at least 1: " + durability);
        }
        if (slots != null && slots < 0) {
            throw new IllegalArgumentException("Slots cannot be negative: " + slots);
        }
        if (capacity != null && capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + capacity);
        }
    }

    public Optional<String> traitId() {
        return Optional.ofNullable(trait);
    }

    public ToolSpec withTierOffset(int offset) {
        return new ToolSpec(offset, speed, durability, slots, capacity, trait, traitBindings, capacityModifier);
    }

    /** The same tool with a Trait: its id, its Capacity modifier and its bindings. */
    public ToolSpec withTrait(String id, int capacityModifier, TraitBinding... bindings) {
        return new ToolSpec(tierOffset, speed, durability, slots, capacity, id, List.of(bindings), capacityModifier);
    }
}
