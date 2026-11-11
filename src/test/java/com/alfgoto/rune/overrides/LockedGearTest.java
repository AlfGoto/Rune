package com.alfgoto.rune.overrides;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class LockedGearTest {

    @Test
    void lockedTools() {
        for (Material m : new Material[] {
            Material.STONE_PICKAXE, Material.IRON_AXE, Material.GOLDEN_SHOVEL, Material.DIAMOND_PICKAXE,
            Material.NETHERITE_SWORD, Material.IRON_HOE
        }) {
            assertTrue(LockedGear.isLocked(m), m.name());
        }
    }

    @Test
    void lockedArmor() {
        for (Material m : new Material[] {
            Material.LEATHER_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE,
            Material.GOLDEN_LEGGINGS, Material.DIAMOND_BOOTS, Material.NETHERITE_HELMET
        }) {
            assertTrue(LockedGear.isLocked(m), m.name());
        }
    }

    @Test
    void woodenAndOtherItemsStayFree() {
        for (Material m : new Material[] {
            Material.WOODEN_PICKAXE, Material.WOODEN_SWORD, Material.WOODEN_AXE, Material.WOODEN_SHOVEL,
            Material.WOODEN_HOE, Material.STONE, Material.IRON_INGOT, Material.DIAMOND, Material.BOW
        }) {
            assertFalse(LockedGear.isLocked(m), m.name());
        }
    }
}
