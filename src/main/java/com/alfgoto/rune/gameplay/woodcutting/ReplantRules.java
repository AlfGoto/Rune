package com.alfgoto.rune.gameplay.woodcutting;

import java.util.Locale;
import java.util.Optional;
import org.bukkit.Material;

/** The id of Replant and which sapling grows back for which log. No world in here, so it can be tested. */
public final class ReplantRules {

    /** Effect id of Replant: on when its value is at least 1. */
    public static final String REPLANT = "replant";

    private ReplantRules() {}

    /**
     * The sapling (or propagule, or fungus) that replaces a Felled Tree whose lowest log is {@code log}: the
     * same wood type, stripped or not. Empty when the material is not a log, or when this Minecraft version has
     * no sapling for it.
     */
    public static Optional<Material> saplingFor(Material log) {
        String name = log.name().toUpperCase(Locale.ROOT);
        if (name.startsWith("STRIPPED_")) {
            name = name.substring("STRIPPED_".length());
        }
        for (String suffix : new String[] {"_LOG", "_WOOD"}) {
            if (name.endsWith(suffix)) {
                String wood = name.substring(0, name.length() - suffix.length());
                return Optional.ofNullable(
                        Material.getMaterial(wood.equals("MANGROVE") ? "MANGROVE_PROPAGULE" : wood + "_SAPLING"));
            }
        }
        for (String suffix : new String[] {"_STEM", "_HYPHAE"}) {
            if (name.endsWith(suffix)) {
                return Optional.ofNullable(
                        Material.getMaterial(name.substring(0, name.length() - suffix.length()) + "_FUNGUS"));
            }
        }
        return Optional.empty();
    }
}
