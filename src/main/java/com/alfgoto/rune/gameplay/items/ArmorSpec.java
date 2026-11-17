package com.alfgoto.rune.gameplay.items;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * What makes a {@link RuneItem} a Rune Armor piece: its kind and the overrides of its stats. A
 * {@code null} override means "default from Rarity" (see {@link ArmorStats}).
 *
 * @param kind the piece (helmet, chestplate, leggings, boots)
 * @param defense protection (armor points) override
 * @param toughness armor toughness override
 * @param durability durability override
 * @param slots Rune slots override
 * @param capacity fixed Capacity override
 * @param trait id of the piece's Trait (shown in the lore), or {@code null} for none
 * @param traitBindings the Trait bindings: what the Trait does (empty for none)
 * @param capacityModifier added to the Capacity (negative = drawback)
 * @param equipment id of the worn-layer equipment asset (e.g. {@code rune:mining_common}), or {@code null}
 *     for the vanilla look
 */
public record ArmorSpec(
        ArmorKind kind,
        @Nullable Integer defense,
        @Nullable Double toughness,
        @Nullable Integer durability,
        @Nullable Integer slots,
        @Nullable Integer capacity,
        @Nullable String trait,
        List<TraitBinding> traitBindings,
        int capacityModifier,
        @Nullable String equipment) {

    /** A piece with a Trait id only (no bindings, no Capacity change). */
    public ArmorSpec(
            ArmorKind kind,
            @Nullable Integer defense,
            @Nullable Double toughness,
            @Nullable Integer durability,
            @Nullable Integer slots,
            @Nullable Integer capacity,
            @Nullable String trait) {
        this(kind, defense, toughness, durability, slots, capacity, trait, List.of(), 0, null);
    }

    public ArmorSpec {
        traitBindings = List.copyOf(traitBindings);
        java.util.Objects.requireNonNull(kind, "kind");
        if (defense != null && defense < 0) {
            throw new IllegalArgumentException("Defense cannot be negative: " + defense);
        }
        if (toughness != null && toughness < 0) {
            throw new IllegalArgumentException("Toughness cannot be negative: " + toughness);
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

    /** A plain piece of {@code kind}: every stat from its Rarity. */
    public static ArmorSpec of(ArmorKind kind) {
        return new ArmorSpec(kind, null, null, null, null, null, null);
    }

    /** The same piece with a Trait: its id, its Capacity modifier and its bindings. */
    public ArmorSpec withTrait(String id, int capacityModifier, TraitBinding... bindings) {
        return new ArmorSpec(
                kind, defense, toughness, durability, slots, capacity, id, List.of(bindings), capacityModifier, equipment);
    }

    /** The same piece with the worn-layer equipment asset {@code assetId} (e.g. {@code rune:mining_common}). */
    public ArmorSpec withEquipment(String assetId) {
        return new ArmorSpec(
                kind, defense, toughness, durability, slots, capacity, trait, traitBindings, capacityModifier, assetId);
    }

    public Optional<String> equipmentId() {
        return Optional.ofNullable(equipment);
    }

    public Optional<String> traitId() {
        return Optional.ofNullable(trait);
    }
}
