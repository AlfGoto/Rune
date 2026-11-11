package com.alfgoto.rune.foundations.blocktracking;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Stores tags in the chunk's PersistentDataContainer: one long array per tag,
 * each long is a chunk-local position. Survives restarts.
 */
public final class PdcBlockTracker implements BlockTracker {

    private final String namespace;

    public PdcBlockTracker(String namespace) {
        this.namespace = namespace;
    }

    private NamespacedKey key(BlockTag tag) {
        return new NamespacedKey(namespace, "block_tag_" + tag.id());
    }

    /** Chunk-local position: height above the world minimum, then x, then z. */
    static long pack(Block block) {
        long y = block.getY() - block.getWorld().getMinHeight();
        return (y << 8) | ((long) (block.getX() & 15) << 4) | (block.getZ() & 15);
    }

    private static PersistentDataContainer pdc(Block block) {
        return block.getChunk().getPersistentDataContainer();
    }

    @Override
    public boolean has(Block block, BlockTag tag) {
        long[] positions = pdc(block).get(key(tag), PersistentDataType.LONG_ARRAY);
        if (positions == null) {
            return false;
        }
        long packed = pack(block);
        for (long position : positions) {
            if (position == packed) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<BlockTag> tags(Block block) {
        Set<BlockTag> found = EnumSet.noneOf(BlockTag.class);
        for (BlockTag tag : BlockTag.values()) {
            if (has(block, tag)) {
                found.add(tag);
            }
        }
        return found;
    }

    @Override
    public void add(Block block, BlockTag tag) {
        PersistentDataContainer pdc = pdc(block);
        long[] positions = pdc.get(key(tag), PersistentDataType.LONG_ARRAY);
        long packed = pack(block);
        if (positions == null) {
            pdc.set(key(tag), PersistentDataType.LONG_ARRAY, new long[] {packed});
            return;
        }
        for (long position : positions) {
            if (position == packed) {
                return;
            }
        }
        long[] grown = Arrays.copyOf(positions, positions.length + 1);
        grown[positions.length] = packed;
        pdc.set(key(tag), PersistentDataType.LONG_ARRAY, grown);
    }

    @Override
    public void remove(Block block, BlockTag tag) {
        PersistentDataContainer pdc = pdc(block);
        long[] positions = pdc.get(key(tag), PersistentDataType.LONG_ARRAY);
        if (positions == null) {
            return;
        }
        long packed = pack(block);
        long[] kept = Arrays.stream(positions).filter(p -> p != packed).toArray();
        if (kept.length == positions.length) {
            return;
        }
        if (kept.length == 0) {
            pdc.remove(key(tag));
        } else {
            pdc.set(key(tag), PersistentDataType.LONG_ARRAY, kept);
        }
    }
}
