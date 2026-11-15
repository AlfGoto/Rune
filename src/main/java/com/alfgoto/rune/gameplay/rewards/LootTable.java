package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A named list of weighted entries. Built with {@link #builder(String)} and registered by id. */
public final class LootTable implements Identified {

    /** One entry: a reward, its base weight and the Rarity of the entry (the odds shift by it). */
    public record Entry(Reward reward, double baseWeight, Rarity rarity) {
        public Entry {
            Objects.requireNonNull(reward, "reward");
            Objects.requireNonNull(rarity, "rarity");
            if (!Double.isFinite(baseWeight) || baseWeight <= 0) {
                throw new IllegalArgumentException("Base weight must be greater than 0, got " + baseWeight);
            }
        }
    }

    private final String id;
    private final List<Entry> entries;

    private LootTable(String id, List<Entry> entries) {
        this.id = id;
        this.entries = List.copyOf(entries);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    @Override
    public String id() {
        return id;
    }

    public List<Entry> entries() {
        return entries;
    }

    public static final class Builder {
        private final String id;
        private final List<Entry> entries = new ArrayList<>();

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder add(Reward reward, double baseWeight, Rarity rarity) {
            entries.add(new Entry(reward, baseWeight, rarity));
            return this;
        }

        public LootTable build() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("Loot table id must not be blank");
            }
            if (entries.isEmpty()) {
                throw new IllegalArgumentException("Loot table '" + id + "' has no entry");
            }
            return new LootTable(id, entries);
        }
    }
}
