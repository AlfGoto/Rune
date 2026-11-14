package com.alfgoto.rune.text;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * The five Rarities shared by Runes, gear, Rune Caches and Quest Tiers.
 * This is the only place that maps a Rarity to its colour.
 */
public enum Rarity {
    COMMON("common", "Common", NamedTextColor.WHITE),
    UNCOMMON("uncommon", "Uncommon", NamedTextColor.GREEN),
    RARE("rare", "Rare", NamedTextColor.AQUA),
    EPIC("epic", "Epic", NamedTextColor.LIGHT_PURPLE),
    LEGENDARY("legendary", "Legendary", NamedTextColor.GOLD);

    private final String id;
    private final String englishName;
    private final TextColor color;

    Rarity(String id, String englishName, TextColor color) {
        this.id = id;
        this.englishName = englishName;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public String englishName() {
        return englishName;
    }

    public TextColor color() {
        return color;
    }

    /** The Rarity with this id (case-insensitive), or {@code null} when there is none. */
    public static Rarity fromId(String id) {
        for (Rarity rarity : values()) {
            if (rarity.id.equalsIgnoreCase(id)) {
                return rarity;
            }
        }
        return null;
    }

    /** Translation key of the Rarity name, e.g. {@code rune.rarity.common}. */
    public String translationKey() {
        return Text.key("rarity", id);
    }
}
