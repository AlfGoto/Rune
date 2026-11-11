package com.alfgoto.rune.overrides;

import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Decides what counts as Locked Gear: vanilla stone, iron, gold, diamond and netherite tools and
 * weapons, and all vanilla armor (leather, chainmail included). Wooden tools are not locked.
 */
public final class LockedGear {

    private static final Set<String> TOOL_PREFIXES = Set.of("STONE_", "IRON_", "GOLDEN_", "DIAMOND_", "NETHERITE_");
    private static final Set<String> TOOL_SUFFIXES = Set.of("_SWORD", "_PICKAXE", "_AXE", "_SHOVEL", "_HOE");
    private static final Set<String> ARMOR_PREFIXES =
            Set.of("LEATHER_", "CHAINMAIL_", "IRON_", "GOLDEN_", "DIAMOND_", "NETHERITE_");
    private static final Set<String> ARMOR_SUFFIXES = Set.of("_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS");

    private LockedGear() {}

    public static boolean isLocked(ItemStack item) {
        return item != null && isLocked(item.getType());
    }

    public static boolean isLocked(Material material) {
        String name = material.name();
        return matches(name, TOOL_PREFIXES, TOOL_SUFFIXES) || matches(name, ARMOR_PREFIXES, ARMOR_SUFFIXES);
    }

    private static boolean matches(String name, Set<String> prefixes, Set<String> suffixes) {
        return prefixes.stream().anyMatch(name::startsWith) && suffixes.stream().anyMatch(name::endsWith);
    }
}
