package com.alfgoto.rune.gameplay.runes;

import java.util.Locale;

/** A place a Rune can be installed: the player, a tool type or an armor piece. */
public enum Fit {
    PLAYER,
    PICKAXE,
    AXE,
    SHOVEL,
    SWORD,
    BOW,
    CROSSBOW,
    TRIDENT,
    FISHING_ROD,
    SHIELD,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS;

    /** Lower case id, e.g. {@code fishing_rod}. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** English name, e.g. {@code Fishing Rod}. */
    public String englishName() {
        StringBuilder name = new StringBuilder();
        for (String word : id().split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    /** Translation key, e.g. {@code rune.fit.pickaxe}. */
    public String translationKey() {
        return "rune.fit." + id();
    }
}
