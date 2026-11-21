package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.config.SpeedRuneConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectBinding;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.effects.RankValues;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.SpeedBonuses;
import com.alfgoto.rune.gameplay.runes.SpeedModule;
import com.alfgoto.rune.gameplay.woodcutting.LeafRules;
import com.alfgoto.rune.gameplay.woodcutting.ReplantRules;
import com.alfgoto.rune.gameplay.woodcutting.TreeDash;
import com.alfgoto.rune.gameplay.woodcutting.WoodcuttingModule;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * The passive axe Runes of docs/jobs/woodcutting.md: Specialist: &lt;wood&gt;, Leaf Fall, Orchard and Canopy
 * Stride. Specialist uses the speed numbers of Specialist: &lt;ore&gt; (config {@code runes.speed.specialist});
 * the others read {@code runes.<id>} from config.yml. The effects themselves run in gameplay.woodcutting.
 */
@AutoService(RuneModule.class)
public final class WoodcuttingRunesModule implements RuneModule {

    /** The wood types of Specialist: &lt;wood&gt; (overworld ids, then Nether ids). */
    static final Map<String, Set<Material>> WOODS = woods();

    private static final Set<Fit> AXE = Set.of(Fit.AXE);

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                RunesModule.class, EffectsModule.class, SpeedModule.class, WoodcuttingModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RuneRegistry runes = ctx.get(RuneRegistry.class);
        EffectRegistry effects = ctx.get(EffectRegistry.class);
        SpeedBonuses speed = ctx.get(SpeedBonuses.class);
        SpeedRuneConfig speedConfig = ctx.config().speedRunes();

        // Specialist: on one wood type, through SpeedBonuses (the speed is applied by SpeedListener).
        for (Map.Entry<String, Set<Material>> wood : WOODS.entrySet()) {
            String id = "specialist_" + wood.getKey();
            speed.register(id, (player, block) -> wood.getValue().contains(block.getType()));
            register(
                    runes,
                    effects,
                    id,
                    Rarity.COMMON,
                    value -> value.with(
                            speedConfig.specialistPerRank(),
                            speedConfig.specialistPerRank(),
                            speedConfig.specialistMaxRank()));
        }

        // Leaf Fall, Orchard and Canopy Stride: numbers from config.yml ("runes"). Canopy Stride has no Max Rank.
        configured(ctx, LeafRules.LEAF_FALL, Rarity.UNCOMMON, true);
        configured(ctx, LeafRules.ORCHARD, Rarity.UNCOMMON, true);
        configured(ctx, LeafRules.CANOPY_STRIDE, Rarity.UNCOMMON, false);
        // Replant (on/off: Max Rank 1) is applied by ReplantListener.
        configured(ctx, ReplantRules.REPLANT, Rarity.RARE, true);
        treeDash(ctx);
    }

    /**
     * Tree Dash, an Active Rune: the value is the range in blocks (config runes.tree_dash: base, per-rank,
     * max-rank, cooldown, speed-per-block, max-speed). It needs a visible log in range, or nothing happens and
     * there is no cooldown.
     */
    private static void treeDash(RuneContext ctx) {
        String id = TreeDash.TREE_DASH;
        var config = ctx.config();
        var base = config.runeNumber(id, "base");
        var perRank = config.runeNumber(id, "per-rank");
        var maxRank = config.runeNumber(id, "max-rank");
        var cooldown = config.runeNumber(id, "cooldown");
        var speedPerBlock = config.runeNumber(id, "speed-per-block");
        var maxSpeed = config.runeNumber(id, "max-speed");
        if (base.isEmpty()
                || perRank.isEmpty()
                || maxRank.isEmpty()
                || cooldown.isEmpty()
                || speedPerBlock.isEmpty()
                || maxSpeed.isEmpty()) {
            ctx.logger().severe("Config 'runes." + id + "' needs base, per-rank, max-rank, cooldown, speed-per-block"
                    + " and max-speed numbers. Rune '" + id + "' is not registered.");
            return;
        }
        // One scan per click: `when` finds the log, `does` (same tick, same click) uses it.
        Map<UUID, Block> found = new HashMap<>();
        Effect<RuneClickEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(id, EffectTriggers.CLICK)
                        .when(context -> {
                            var log = TreeDash.nearestVisibleLog(context.player(), context.value());
                            if (log.isPresent()) {
                                found.put(context.player().getUniqueId(), log.get());
                            } else {
                                found.remove(context.player().getUniqueId());
                            }
                            return log.isPresent();
                        })
                        .does(context -> {
                            Block log = found.remove(context.player().getUniqueId());
                            if (log != null) {
                                TreeDash.dash(
                                        context.player(), log, speedPerBlock.getAsDouble(), maxSpeed.getAsDouble());
                            }
                        }));
        // No Rune texture is drawn yet: shares the test model (see docs/design-decisions.md, Textures).
        ctx.get(RuneRegistry.class)
                .register(Rune.active(
                        new RuneItem(id, Rarity.RARE, Material.PAPER, "test_rune"),
                        effect.with(
                                base.getAsDouble(),
                                perRank.getAsDouble(),
                                (int) Math.max(1, maxRank.getAsDouble())),
                        AXE,
                        null,
                        RankValues.of(cooldown.getAsDouble(), 0)));
    }

    private static void configured(RuneContext ctx, String id, Rarity rarity, boolean hasMaxRank) {
        var config = ctx.config();
        var base = config.runeNumber(id, "base");
        var perRank = config.runeNumber(id, "per-rank");
        var maxRank = config.runeNumber(id, "max-rank");
        if (base.isEmpty() || perRank.isEmpty() || (hasMaxRank && maxRank.isEmpty())) {
            ctx.logger().severe("Config 'runes." + id + "' needs base and per-rank" + (hasMaxRank ? " and max-rank" : "")
                    + " numbers. Rune '" + id + "' is not registered.");
            return;
        }
        register(
                ctx.get(RuneRegistry.class),
                ctx.get(EffectRegistry.class),
                id,
                rarity,
                effect -> hasMaxRank
                        ? effect.with(
                                base.getAsDouble(),
                                perRank.getAsDouble(),
                                (int) Math.max(1, maxRank.getAsDouble()))
                        : effect.with(base.getAsDouble(), perRank.getAsDouble()));
    }

    private static void register(
            RuneRegistry runes,
            EffectRegistry effects,
            String id,
            Rarity rarity,
            java.util.function.Function<Effect<RuneBlockBreakEvent>, EffectBinding<RuneBlockBreakEvent>> bind) {
        // The effect is applied elsewhere (SpeedListener, gameplay.woodcutting), so the Effect itself does nothing.
        Effect<RuneBlockBreakEvent> effect =
                effects.register(Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {}));
        // No Rune texture is drawn yet: shares the test model (see docs/design-decisions.md, Textures).
        runes.register(new Rune(new RuneItem(id, rarity, Material.PAPER, "test_rune"), bind.apply(effect), AXE));
    }

    private static Map<String, Set<Material>> woods() {
        Map<String, Set<Material>> woods = new LinkedHashMap<>();
        for (String id : new String[] {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "poplar"
        }) {
            woods.put(id, family(id, "LOG", "WOOD"));
        }
        for (String id : new String[] {"crimson", "warped"}) {
            woods.put(id, family(id, "STEM", "HYPHAE"));
        }
        return woods;
    }

    /** The log, the wood and their stripped versions of one wood type. A block that does not exist is skipped. */
    private static Set<Material> family(String id, String log, String wood) {
        Set<Material> set = EnumSet.noneOf(Material.class);
        String name = id.toUpperCase(java.util.Locale.ROOT);
        for (String candidate : new String[] {
            name + "_" + log, name + "_" + wood, "STRIPPED_" + name + "_" + log, "STRIPPED_" + name + "_" + wood
        }) {
            Material material = Material.getMaterial(candidate);
            if (material != null) {
                set.add(material);
            }
        }
        return set;
    }
}
