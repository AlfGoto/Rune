package com.alfgoto.rune.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.Material;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.junit.jupiter.api.Test;

/** Issue #245: Rune tools, weapons and armor sit in the Tools tab of the vanilla recipe book. */
class RecipeCategoryTest {

    @Test
    void toolsWeaponsAndArmorAreEquipment() {
        for (Material material : new Material[] {
            Material.WOODEN_PICKAXE, Material.IRON_AXE, Material.STONE_SHOVEL, Material.GOLDEN_HOE,
            Material.DIAMOND_SWORD, Material.IRON_HELMET, Material.LEATHER_CHESTPLATE,
            Material.NETHERITE_LEGGINGS, Material.CHAINMAIL_BOOTS
        }) {
            assertEquals(CraftingBookCategory.EQUIPMENT, RecipeBook.category(material), material.name());
        }
    }

    @Test
    void componentsAndOtherItemsAreMisc() {
        for (Material material : new Material[] {Material.PAPER, Material.STICK, Material.IRON_INGOT}) {
            assertEquals(CraftingBookCategory.MISC, RecipeBook.category(material), material.name());
        }
    }
}
