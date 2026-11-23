package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.LootRoller;
import com.alfgoto.rune.gameplay.rewards.LootTable;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
 * {@code RuneReward}, not here). Opening fires a {@link CacheOpenEvent} and gives the loot {@code revealTicks}
 * later, so the Cache reel can reveal it first; a player who leaves in the meantime gets it at once.
 */
public final class Caches {

    /** The loot table every Cache rolls. */
    public static final String TABLE = "cache";

    /** Ticks between opening a Cache and getting its loot in game: the length of the reel. */
    public static final int REVEAL_TICKS = 50;

    private final ItemRegistry items;
    private final LootTableRegistry tables;
    private final LootRoller roller;
    private final RandomGenerator random;
    private final NamespacedKey jobKey;
    private final Plugin plugin;
    private final int revealTicks;
    private final List<Pending> pending = new ArrayList<>();

    /** Loot given at once when a Cache is opened. */
    public Caches(
            Plugin plugin, ItemRegistry items, LootTableRegistry tables, LootRoller roller, RandomGenerator random) {
        this(plugin, items, tables, roller, random, 0);
    }

    /** Loot given {@code revealTicks} after a Cache is opened. */
    public Caches(
            Plugin plugin,
            ItemRegistry items,
            LootTableRegistry tables,
            LootRoller roller,
            RandomGenerator random,
            int revealTicks) {
        this.plugin = plugin;
        this.revealTicks = revealTicks;
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
        Bukkit.getPluginManager().callEvent(new CacheOpenEvent(player, rarity, drops, revealTicks));
        if (revealTicks <= 0) {
            give(player, rarity, drops);
            return true;
        }
        Pending later = new Pending(player.getUniqueId(), rarity, drops);
        pending.add(later);
        Bukkit.getScheduler().runTaskLater(plugin, () -> finish(later), revealTicks);
        return true;
    }

    /** Gives at once everything {@code player} still waits for (they are leaving). */
    void giveNow(Player player) {
        for (Pending p : List.copyOf(pending)) {
            if (p.player().equals(player.getUniqueId()) && pending.remove(p)) {
                give(player, p.rarity(), p.drops());
            }
        }
    }

    /** Gives everything still waiting to the players still online (the plugin stops). */
    void giveAllNow() {
        for (Pending p : List.copyOf(pending)) {
            Player player = Bukkit.getPlayer(p.player());
            if (player != null && pending.remove(p)) {
                give(player, p.rarity(), p.drops());
            }
        }
        pending.clear();
    }

    private void finish(Pending later) {
        Player player = Bukkit.getPlayer(later.player());
        if (player != null && pending.remove(later)) {
            give(player, later.rarity(), later.drops());
        }
    }

    private record Pending(UUID player, Rarity rarity, List<Drop> drops) {}

    private static void give(Player player, Rarity rarity, List<Drop> drops) {
        player.sendMessage(Component.translatable(
                        "rune.cache.opened", "You open a Cache: %s result(s)", Component.text(drops.size()))
                .color(rarity.color()));
        for (Drop drop : drops) {
            drop.give(player);
            player.sendMessage(Component.translatable("rune.cache.entry", "- %s", drop.describe())
                    .color(NamedTextColor.GRAY));
        }
    }
}
