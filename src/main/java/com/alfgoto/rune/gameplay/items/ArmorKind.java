package com.alfgoto.rune.gameplay.items;

import java.util.Locale;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;

/**
 * The kind of a Rune Armor piece: where it is worn. The item uses the vanilla leather piece as its
 * base so the game lets the player wear it; its protection is replaced by the Rune Armor stats, and a
 * Rune item is never Locked Gear (see {@code LockedGear}).
 */
public enum ArmorKind {
    HELMET(Material.LEATHER_HELMET, EquipmentSlotGroup.HEAD, EquipmentSlot.HEAD),
    CHESTPLATE(Material.LEATHER_CHESTPLATE, EquipmentSlotGroup.CHEST, EquipmentSlot.CHEST),
    LEGGINGS(Material.LEATHER_LEGGINGS, EquipmentSlotGroup.LEGS, EquipmentSlot.LEGS),
    BOOTS(Material.LEATHER_BOOTS, EquipmentSlotGroup.FEET, EquipmentSlot.FEET);

    private final Material material;
    private final EquipmentSlotGroup slot;
    private final EquipmentSlot equipmentSlot;

    ArmorKind(Material material, EquipmentSlotGroup slot, EquipmentSlot equipmentSlot) {
        this.material = material;
        this.slot = slot;
        this.equipmentSlot = equipmentSlot;
    }

    /** The slot the piece is worn in (for the {@code equippable} component). */
    public EquipmentSlot equipmentSlot() {
        return equipmentSlot;
    }

    /** The vanilla material a Rune Armor piece of this kind is made on. */
    public Material material() {
        return material;
    }

    /** The equipment slot where the piece gives its protection. */
    public EquipmentSlotGroup slot() {
        return slot;
    }

    /** Lower case id, e.g. {@code chestplate}. Also the key in the {@code armor} config. */
    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The kind whose base material is {@code material}, if any. */
    public static Optional<ArmorKind> of(Material material) {
        for (ArmorKind kind : values()) {
            if (kind.material == material) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
