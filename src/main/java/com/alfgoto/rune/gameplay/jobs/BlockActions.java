package com.alfgoto.rune.gameplay.jobs;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Material;

/**
 * Block → Job Action table. The block decides the Job, never the tool. Content fills it
 * (see {@code content.jobs.CoreBlockActions}); {@link BlockActionListener} reads it.
 */
public final class BlockActions {

    private final Map<Material, BlockAction> actions = new EnumMap<>(Material.class);

    /** Sets what breaking {@code material} gives. A block belongs to exactly one Job: a second entry is an error. */
    public void register(Material material, BlockAction action) {
        if (actions.putIfAbsent(material, action) != null) {
            throw new IllegalStateException("Block " + material + " already has a Job Action");
        }
    }

    public Optional<BlockAction> get(Material material) {
        return Optional.ofNullable(actions.get(material));
    }

    public int size() {
        return actions.size();
    }
}
