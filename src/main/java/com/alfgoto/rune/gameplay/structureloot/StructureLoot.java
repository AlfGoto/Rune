package com.alfgoto.rune.gameplay.structureloot;

import com.alfgoto.rune.foundations.config.StructureLootConfig;
import com.alfgoto.rune.gameplay.blueprints.Blueprint;
import com.alfgoto.rune.gameplay.blueprints.Blueprints;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

/**
 * Relics and Blueprints injected into structure chests, per loot table key, with a Job Level gate (config
 * {@code structure-loot}). A roll only gives an entry to a player who meets its gate: nothing for the others.
 * An item or Blueprint that is not registered yet is skipped, and so is a Blueprint the player already used.
 */
public final class StructureLoot {

    /** One injected item and the name to show for it. */
    public record Result(ItemStack stack, Component name) {}

    private final StructureLootConfig config;
    private final JobRegistry jobs;
    private final JobProgress progress;
    private final ItemRegistry items;
    private final Blueprints blueprints;

    public StructureLoot(
            StructureLootConfig config,
            JobRegistry jobs,
            JobProgress progress,
            ItemRegistry items,
            Blueprints blueprints) {
        this.config = config;
        this.jobs = jobs;
        this.progress = progress;
        this.items = items;
        this.blueprints = blueprints;
    }

    /** The loot table keys that have at least one entry. */
    public Set<String> tables() {
        Set<String> out = new TreeSet<>();
        config.entries().forEach(entry -> out.add(entry.table()));
        return out;
    }

    /** Rolls every entry of {@code table} for {@code player}. The caller gives the results. */
    public List<Result> roll(UUID player, String table, RandomGenerator random) {
        List<Result> out = new ArrayList<>();
        for (StructureLootConfig.Entry entry : config.entries()) {
            if (!entry.table().equals(table)) {
                continue;
            }
            Job job = jobs.registry().find(entry.job()).orElse(null);
            if (job == null || progress.jobLevel(player, job) < entry.minLevel()) {
                continue;
            }
            if (random.nextDouble() >= entry.chance()) {
                continue;
            }
            Result result = create(entry, player);
            if (result != null) {
                out.add(result);
            }
        }
        return out;
    }

    private Result create(StructureLootConfig.Entry entry, UUID player) {
        if (entry.blueprint()) {
            Blueprint blueprint = blueprints.find(entry.id()).orElse(null);
            if (blueprint == null || blueprints.isUnlocked(player, blueprint.id())) {
                return null;
            }
            Component name = Text.translatable(
                            "rune.reward.blueprint",
                            "Blueprint: %s",
                            Component.translatable(blueprint.translationKey(), blueprint.fallbackName()))
                    .color(Rarity.LEGENDARY.color());
            return new Result(blueprints.create(blueprint), name);
        }
        RuneItem item = items.find(entry.id()).orElse(null);
        if (item == null) {
            return null;
        }
        Component name = Component.translatable(item.translationKey(), item.fallbackName())
                .color(item.rarity().color());
        return new Result(items.create(item, 1), name);
    }
}
