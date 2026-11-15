package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.foundations.registry.Registry;

/** The shared loot table registry. Starts empty: content modules fill it. */
public final class LootTableRegistry {

    private final Registry<LootTable> tables = new Registry<>("loot tables");

    /** Adds a table. Throws if its id is already used. */
    public LootTable register(LootTable table) {
        return tables.register(table);
    }

    /** The underlying registry. Read it when you need it: content modules add tables after enable. */
    public Registry<LootTable> registry() {
        return tables;
    }
}
