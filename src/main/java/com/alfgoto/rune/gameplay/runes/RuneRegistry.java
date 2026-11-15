package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import java.util.Collection;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * All known {@link Rune}s. Shared as a service; content modules fill it (ADR 0004).
 * Registering a Rune also registers its item in the {@link ItemRegistry}. The Rank of a Rune stack is
 * stored in its PersistentDataContainer under {@code rune:rank}.
 */
public final class RuneRegistry {

    private final Registry<Rune> runes = new Registry<>("runes");
    private final ItemRegistry items;
    private final NamespacedKey rankKey;
    private final CapacityCost capacityCost;

    public RuneRegistry(Plugin plugin, ItemRegistry items, CapacityCost capacityCost) {
        this.items = items;
        this.rankKey = new NamespacedKey(plugin, "rank");
        this.capacityCost = capacityCost;
    }

    public Rune register(Rune rune) {
        runes.register(rune);
        items.register(rune.item());
        return rune;
    }

    public Optional<Rune> find(String id) {
        return runes.find(id);
    }

    public Rune get(String id) {
        return runes.get(id);
    }

    /** All Runes, in registration order. Read it when you use it, not at enable time. */
    public Collection<Rune> all() {
        return runes.all();
    }

    /** The Capacity Cost of {@code rune} at {@code rank}. */
    public int capacityCost(Rune rune, int rank) {
        return capacityCost.of(rune.rarity(), rank);
    }

    /** A new Rune stack at {@code rank}. Throws if the Rank is not allowed (see {@link Rune#allowsRank}). */
    public ItemStack create(Rune rune, int rank) {
        if (!rune.allowsRank(rank)) {
            throw new IllegalArgumentException("Rank " + rank + " is not allowed for " + rune.id());
        }
        ItemStack stack = items.create(rune.item(), 1);
        items.update(stack, meta -> meta.getPersistentDataContainer().set(rankKey, PersistentDataType.INTEGER, rank));
        return stack;
    }

    /** The Rune this stack is, or empty for any other stack. */
    public Optional<Rune> of(ItemStack stack) {
        return items.of(stack).flatMap(item -> runes.find(item.id()));
    }

    /** The Rank of a Rune stack (1 if none is stored). */
    public int rankOf(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return 1;
        }
        Integer rank = stack.getItemMeta().getPersistentDataContainer().get(rankKey, PersistentDataType.INTEGER);
        return rank == null || rank < 1 ? 1 : rank;
    }
}
