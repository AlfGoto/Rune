package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.LootRoller;
import com.alfgoto.rune.gameplay.rewards.LootTable;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Rarity;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * Rune Caches: sealed items with a Rarity (one item per Rarity, {@code rune_cache_<rarity>}) and an optional
 * Job id. Opening one takes it from the stack, rolls the shared {@code cache} loot table with the Cache
 * Rarity and Job, and gives the loot. A Job Cache leans towards that Job's Runes (the bias is in
 * {@code RuneReward}, not here). Opening is instant for now; the reel menu comes later.
 */
public final class Caches {

    /** The loot table every Cache rolls. */
    public static final String TABLE = "cache";

    private final ItemRegistry items;
    private final LootTableRegistry tables;
    private final LootRoller roller;
    private final RandomGenerator random;
    private final NamespacedKey jobKey;

    public Caches(
            Plugin plugin, ItemRegistry items, LootTableRegistry tables, LootRoller roller, RandomGenerator random) {
        this.items = items;
        this.tables = tables;
        this.roller = roller;
        this.random = random;
        this.jobKey = new NamespacedKey(plugin, "cache_job");
    }

    /** The item id of the Cache of this Rarity, e.g. {@code rune_cache_rare}. */
    public static String itemId(Rarity rarity) {
        return "rune_cache_" + rarity.id();
    }

    /** A new Cache of {@code rarity}, for {@code jobId} (null = no Job). */
    public ItemStack create(Rarity rarity, @Nullable String jobId) {
        ItemStack stack = items.create(items.get(itemId(rarity)), 1);
        if (jobId != null) {
            items.update(
                    stack, meta -> meta.getPersistentDataContainer().set(jobKey, PersistentDataType.STRING, jobId));
        }
        return stack;
    }

    /** The Rarity of a Cache stack, or empty when it is not a Cache. */
    public Optional<Rarity> rarityOf(@Nullable ItemStack stack) {
        return items.of(stack)
                .flatMap(item -> Arrays.stream(Rarity.values())
                        .filter(rarity -> itemId(rarity).equals(item.id()))
                        .findFirst());
    }

    /** The Job id of a Cache stack, or empty for a general Cache. */
    public Optional<String> jobOf(@Nullable ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                stack.getItemMeta().getPersistentDataContainer().get(jobKey, PersistentDataType.STRING));
    }

    /**
     * Opens one Cache of the stack: takes it, rolls the table, gives each Drop and tells the player.
     * Returns false (and takes nothing) when the stack is not a Cache or the table is missing.
     */
    public boolean open(Player player, ItemStack stack) {
        Rarity rarity = rarityOf(stack).orElse(null);
        LootTable table = tables.registry().find(TABLE).orElse(null);
        if (rarity == null || table == null) {
            return false;
        }
        RollContext context = new RollContext(player.getUniqueId(), rarity, jobOf(stack).orElse(null));
        stack.setAmount(stack.getAmount() - 1);
        List<Drop> drops = roller.roll(table, context, random);
        player.sendMessage(Component.translatable(
                        "rune.cache.opened", "You open a Cache: %s result(s)", Component.text(drops.size()))
                .color(rarity.color()));
        for (Drop drop : drops) {
            drop.give(player);
            player.sendMessage(Component.translatable("rune.cache.entry", "- %s", drop.describe())
                    .color(NamedTextColor.GRAY));
        }
        return true;
    }
}
