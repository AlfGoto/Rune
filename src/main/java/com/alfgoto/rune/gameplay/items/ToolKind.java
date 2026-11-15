package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.triggers.ClickType;
import java.util.List;
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
    SHOVEL(Material.WOODEN_SHOVEL, Tag.MINEABLE_SHOVEL);

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

    /**
     * The clicks of the Active Slots, in Rune slot order: slot 1 is the first click, slot 2 the second.
     * Today's kinds all have two. Tools whose right-click in air is already used (bow, crossbow, trident,
     * rod, shield) will have only {@link ClickType#LEFT} when they are built.
     */
    public List<ClickType> activeClicks() {
        return List.of(ClickType.RIGHT, ClickType.LEFT);
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
