package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.config.SpeedRuneConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlacesModule;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.SpeedBonuses;
import com.alfgoto.rune.gameplay.runes.SpeedModule;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.bukkit.Material;

/**
 * The mining speed Runes of docs/jobs/mining.md: Haste, Specialist: &lt;ore&gt; and Affinity: &lt;place&gt;.
 * Each variant is its own Rune with its own effect id, so they rank up separately and stack.
 */
@AutoService(RuneModule.class)
public final class SpeedRunesModule implements RuneModule {

    /** Specialist ores (id, matching blocks). The 8 common overworld ores. */
    static final Map<String, Set<Material>> ORES = new TreeMap<>(Map.of(
            "coal", Set.of(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE),
            "copper", Set.of(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE),
            "iron", Set.of(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE),
            "gold", Set.of(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE),
            "redstone", Set.of(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE),
            "lapis", Set.of(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE),
            "diamond", Set.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
            "emerald", Set.of(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE)));

    /** Affinity places: the Places that exist today (see CorePlaces). More come with their Places. */
    static final List<String> PLACES = List.of("lush_caves", "taiga", "mineshaft", "deep_dark");

    /** Forest Places: their Affinity fits axes only. Other Places fit pickaxes only (docs/jobs/mining.md). */
    static final Set<String> FOREST_PLACES = Set.of("taiga");

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, EffectsModule.class, SpeedModule.class, PlacesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RuneRegistry runes = ctx.get(RuneRegistry.class);
        EffectRegistry effects = ctx.get(EffectRegistry.class);
        SpeedBonuses speed = ctx.get(SpeedBonuses.class);
        PlaceDetector detector = ctx.get(PlaceDetector.class);
        SpeedRuneConfig config = ctx.config().speedRunes();

        // Haste: on any block.
        speed.register("haste", (player, block) -> true);
        register(
                runes,
                effects,
                "haste",
                Rarity.COMMON,
                config.hastePerRank(),
                config.hasteMaxRank(),
                Set.of(Fit.PICKAXE, Fit.AXE, Fit.SHOVEL));

        // Specialist: on one ore. Pickaxe only.
        for (Map.Entry<String, Set<Material>> ore : ORES.entrySet()) {
            String id = "specialist_" + ore.getKey();
            speed.register(id, (player, block) -> ore.getValue().contains(block.getType()));
            register(
                    runes,
                    effects,
                    id,
                    Rarity.COMMON,
                    config.specialistPerRank(),
                    config.specialistMaxRank(),
                    Set.of(Fit.PICKAXE));
        }

        // Affinity: while the player stands in one Place. Pickaxe in cave and mining Places, axe in forest Places.
        for (String place : PLACES) {
            String id = "affinity_" + place;
            speed.register(
                    id,
                    (player, block) ->
                            detector.placesOf(player).stream().anyMatch(p -> p.id().equals(place)));
            register(
                    runes,
                    effects,
                    id,
                    Rarity.UNCOMMON,
                    config.affinityPerRank(),
                    config.affinityMaxRank(),
                    FOREST_PLACES.contains(place) ? Set.of(Fit.AXE) : Set.of(Fit.PICKAXE));
        }

        deepstride(ctx, runes, effects, speed);
    }

    /**
     * Deepstride (docs/jobs/mining.md): faster mining below a depth line. The Rune's value is the line: Rank 1
     * works below Y 0 and each Rank moves the line up by {@code runes.deepstride.per-rank} (16). No Max Rank.
     * Each installed Deepstride adds {@code runes.deepstride.speed} percent while the player is below its line.
     */
    private static void deepstride(RuneContext ctx, RuneRegistry runes, EffectRegistry effects, SpeedBonuses speed) {
        var config = ctx.config();
        var base = config.runeNumber("deepstride", "base");
        var perRank = config.runeNumber("deepstride", "per-rank");
        var percent = config.runeNumber("deepstride", "speed");
        if (base.isEmpty() || perRank.isEmpty() || percent.isEmpty() || percent.getAsDouble() <= 0) {
            ctx.logger()
                    .severe("Config 'runes.deepstride' needs base, per-rank and speed (above 0) numbers. "
                            + "Rune 'deepstride' is not registered.");
            return;
        }
        double bonus = percent.getAsDouble();
        speed.registerByValue("deepstride", (player, block, line) -> player.getLocation().getY() < line ? bonus : 0);
        Effect<RuneBlockBreakEvent> effect =
                effects.register(Effect.builder("deepstride", EffectTriggers.BLOCK_BREAK).does(context -> {}));
        runes.register(new Rune(
                new RuneItem("deepstride", Rarity.UNCOMMON, Material.PAPER, "deepstride"),
                effect.with(base.getAsDouble(), perRank.getAsDouble()),
                Set.of(Fit.PICKAXE)));
    }

    private static void register(
            RuneRegistry runes,
            EffectRegistry effects,
            String id,
            Rarity rarity,
            double perRank,
            int maxRank,
            Set<Fit> fits) {
        // The speed is applied by SpeedListener, so the Effect itself does nothing.
        Effect<RuneBlockBreakEvent> effect =
                effects.register(Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {}));
        // Model: assets/rune/items/<id>.json, layered with the Rank (docs/art-direction.md, "Runes").
        runes.register(new Rune(
                new RuneItem(id, rarity, Material.PAPER, id),
                effect.with(perRank, perRank, maxRank),
                fits));
    }
}
