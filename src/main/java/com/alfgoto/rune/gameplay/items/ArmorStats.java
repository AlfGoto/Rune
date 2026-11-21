package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.config.ArmorConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import java.util.Optional;

/**
 * The final stats of a Rune Armor piece: its {@link ArmorSpec} overrides on top of the config defaults.
 * Protection, durability and toughness follow the Rarity (Common ≈ leather … Legendary ≈ netherite).
 * Slots and Capacity follow the Rarity too, with the same table as Rune Tools; Capacity is fixed
 * (Rune Armor has no Mastery).
 */
public record ArmorStats(
        ArmorKind kind,
        int defense,
        double toughness,
        int durability,
        int slots,
        int capacity,
        Optional<String> trait) {

    /** Resolves the stats of {@code item}, or empty when it is not Rune Armor. */
    public static Optional<ArmorStats> of(RuneItem item, ArmorConfig armor, ToolConfig tools) {
        return item.armorSpec().map(spec -> resolve(item, spec, armor, tools));
    }

    private static ArmorStats resolve(RuneItem item, ArmorSpec spec, ArmorConfig armor, ToolConfig tools) {
        int rarity = item.rarity().ordinal();
        String piece = spec.kind().id();
        return new ArmorStats(
                spec.kind(),
                spec.defense() != null ? spec.defense() : armor.defenseByRarity().get(piece).get(rarity),
                spec.toughness() != null ? spec.toughness() : armor.toughnessByRarity().get(rarity),
                spec.durability() != null ? spec.durability() : armor.durabilityByRarity().get(piece).get(rarity),
                spec.slots() != null ? spec.slots() : tools.slotsByRarity().get(rarity),
                // Capacity = base (or override) + the Trait's modifier, never below 0.
                TraitBinding.adjustedCapacity(
                        spec.capacity() != null ? spec.capacity() : tools.capacityByRarity().get(rarity),
                        spec.capacityModifier()),
                spec.traitId());
    }
}
