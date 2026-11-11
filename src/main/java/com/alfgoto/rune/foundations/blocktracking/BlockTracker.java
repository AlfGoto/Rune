package com.alfgoto.rune.foundations.blocktracking;

import java.util.Set;
import org.bukkit.block.Block;

/** Remembers tags on block positions. Main thread only. */
public interface BlockTracker {

    boolean has(Block block, BlockTag tag);

    /** Every tag on this position. Empty if none. */
    Set<BlockTag> tags(Block block);

    void add(Block block, BlockTag tag);

    void remove(Block block, BlockTag tag);

    /** Removes every tag from this position. */
    default void clear(Block block) {
        for (BlockTag tag : BlockTag.values()) {
            remove(block, tag);
        }
    }
}
