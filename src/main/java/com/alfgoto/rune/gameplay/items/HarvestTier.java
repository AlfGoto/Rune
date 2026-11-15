package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.text.Rarity;
import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * Which blocks a Rune Tool can harvest: vanilla's stone / iron / diamond tiers. Defaults from the
 * tool's Rarity; a tool can be one tier above or below its default.
 */
public enum HarvestTier {
    STONE("stone", Tag.INCORRECT_FOR_STONE_TOOL),
    IRON("iron", Tag.INCORRECT_FOR_IRON_TOOL),
    DIAMOND("diamond", Tag.INCORRECT_FOR_DIAMOND_TOOL);

    private final String id;
    private final Tag<Material> incorrectBlocks;

    HarvestTier(String id, Tag<Material> incorrectBlocks) {
        this.id = id;
        this.incorrectBlocks = incorrectBlocks;
    }

    public String id() {
        return id;
    }

    /** The vanilla tag of blocks this tier cannot harvest (they give no drops). */
    public Tag<Material> incorrectBlocks() {
        return incorrectBlocks;
    }

    /** Translation key of the tier name, e.g. {@code rune.harvest_tier.iron}. */
    public String translationKey() {
        return "rune.harvest_tier." + id;
    }

    /** Common = stone, Uncommon = iron, Rare and above = diamond. */
    public static HarvestTier defaultFor(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> STONE;
            case UNCOMMON -> IRON;
            case RARE, EPIC, LEGENDARY -> DIAMOND;
        };
    }

    /**
     * This tier moved by {@code offset} (-1, 0 or +1).
     *
     * @throws IllegalArgumentException if the result is outside stone..diamond
     */
    public HarvestTier shifted(int offset) {
        int index = ordinal() + offset;
        if (index < 0 || index >= values().length) {
            throw new IllegalArgumentException("Harvest Tier " + this + " cannot be shifted by " + offset);
        }
        return values()[index];
    }
}
