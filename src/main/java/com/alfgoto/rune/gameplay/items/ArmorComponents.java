package com.alfgoto.rune.gameplay.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;

/**
 * Puts a Rune Armor piece's stats on its stack with vanilla components, so the game itself gives the
 * protection: the {@code attribute_modifiers} component (armor, toughness, in the
 * piece's slot) replaces the leather defaults, and {@code max_damage} sets the durability.
 */
final class ArmorComponents {

    private ArmorComponents() {}

    static void apply(ItemStack stack, ArmorStats stats) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.itemAttributes();
        add(builder, Attribute.ARMOR, "armor", stats.defense(), stats);
        add(builder, Attribute.ARMOR_TOUGHNESS, "armor_toughness", stats.toughness(), stats);
        stack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        stack.setData(DataComponentTypes.MAX_DAMAGE, stats.durability());
    }

    /** Removes the protection (used while a piece is Broken). */
    static void clear(ItemStack stack) {
        stack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build());
    }

    private static void add(
            ItemAttributeModifiers.Builder builder, Attribute attribute, String name, double amount, ArmorStats stats) {
        if (amount <= 0) {
            return;
        }
        builder.addModifier(
                attribute,
                new AttributeModifier(
                        new NamespacedKey("rune", "armor." + stats.kind().id() + "." + name),
                        amount,
                        AttributeModifier.Operation.ADD_NUMBER,
                        stats.kind().slot()));
    }
}
