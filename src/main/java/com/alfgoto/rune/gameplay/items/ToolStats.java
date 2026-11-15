package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.text.Rarity;
import java.util.Optional;

/**
 * The final stats of a Rune Tool: its {@link ToolSpec} overrides on top of the config defaults.
 * Speed and durability follow the Harvest Tier; Epic and Legendary tools get gold speed and
 * netherite durability whatever their tier. Slots and Capacity follow the Rarity.
 */
public record ToolStats(
        ToolKind kind,
        HarvestTier tier,
        float speed,
        int durability,
        int slots,
        int capacity,
        Optional<String> trait) {

    /** Resolves the stats of {@code item}, or empty when it is not a Rune Tool. */
    public static Optional<ToolStats> of(RuneItem item, ToolConfig config) {
        return item.toolSpec().map(spec -> resolve(item, spec, config));
    }

    private static ToolStats resolve(RuneItem item, ToolSpec spec, ToolConfig config) {
        ToolKind kind = ToolKind.of(item.material()).orElseThrow();
        HarvestTier tier = HarvestTier.defaultFor(item.rarity()).shifted(spec.tierOffset());
        boolean epicPlus = item.rarity().ordinal() >= Rarity.EPIC.ordinal();
        float speed = spec.speed() != null
                ? spec.speed()
                : (float) (epicPlus ? config.epicSpeed() : config.speedByTier().get(tier.ordinal()));
        int durability = spec.durability() != null
                ? spec.durability()
                : (epicPlus ? config.epicDurability() : config.durabilityByTier().get(tier.ordinal()));
        int slots = spec.slots() != null ? spec.slots() : config.slotsByRarity().get(item.rarity().ordinal());
        int capacity =
                spec.capacity() != null ? spec.capacity() : config.capacityByRarity().get(item.rarity().ordinal());
        return new ToolStats(kind, tier, speed, durability, slots, capacity, spec.traitId());
    }
}
