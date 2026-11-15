package com.alfgoto.rune.gameplay.items;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * The kind of a Rune Tool: which blocks it mines fast. The item uses the vanilla wooden tool as
 * its base (wooden tools are not Locked Gear); its mining components are replaced.
 */
public enum ToolKind {
    PICKAXE(Material.WOODEN_PICKAXE, Tag.MINEABLE_PICKAXE),
    AXE(Material.WOODEN_AXE, Tag.MINEABLE_AXE),
    SHOVEL(Material.WOODEN_SHOVEL, Tag.MINEABLE_SHOVEL),
    HOE(Material.WOODEN_HOE, Tag.MINEABLE_HOE);

    private final Material material;
    private final Tag<Material> mineable;

    ToolKind(Material material, Tag<Material> mineable) {
        this.material = material;
        this.mineable = mineable;
    }

    /** The vanilla material a Rune Tool of this kind is made on. */
    public Material material() {
        return material;
    }

    /** The vanilla tag of blocks this kind of tool mines. */
    public Tag<Material> mineable() {
        return mineable;
    }

    /** The kind whose base material is {@code material}, if any. */
    public static Optional<ToolKind> of(Material material) {
        for (ToolKind kind : values()) {
            if (kind.material == material) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
