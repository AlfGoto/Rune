package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.gameplay.effects.RankValues;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.Material;

/** Registers the core Runes into the RuneRegistry. For now only the test Rune. */
@AutoService(RuneModule.class)
public final class CoreRunesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, EffectsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        // The test Rune's effect does nothing: it only gives the Rune something to be built on.
        Effect<RuneBlockBreakEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder("test_rune", EffectTriggers.BLOCK_BREAK).does(context -> {}));
        // Uses the test model of the resource pack (assets/rune/items/test_rune.json).
        ctx.get(RuneRegistry.class)
                .register(new Rune(
                        new RuneItem("test_rune", Rarity.RARE, Material.PAPER, "test_rune"),
                        effect.with(5, 5, 4),
                        Set.of(Fit.PLAYER, Fit.PICKAXE)));
        // The test Active Rune does nothing but start its cooldown (10 s, 2 s less per Rank), so the Active
        // Slots and the action bar can be tested: /rune debug install test_active_rune, then click in air.
        Effect<RuneClickEvent> active = ctx.get(EffectRegistry.class)
                .register(Effect.builder("test_active_rune", EffectTriggers.CLICK).does(context -> {}));
        ctx.get(RuneRegistry.class)
                .register(Rune.active(
                        new RuneItem("test_active_rune", Rarity.RARE, Material.PAPER, "test_rune"),
                        active.with(1, 1, 3),
                        Set.of(Fit.PICKAXE, Fit.AXE, Fit.SHOVEL),
                        null,
                        RankValues.of(10, -2, 3)));

        // Drop-changing Runes (docs/jobs/mining.md). Numbers come from config.yml ("runes"). No Rune texture is
        // drawn yet, so they use the test Rune model.
        Set<Fit> diggers = Set.of(Fit.PICKAXE, Fit.AXE, Fit.SHOVEL);
        dropRune(ctx, "fortune", Rarity.UNCOMMON, diggers);
        dropRune(ctx, "smelt", Rarity.RARE, diggers);
        dropRune(ctx, "silk_touch", Rarity.EPIC, diggers);
    }

    /**
     * Registers a Rune whose effect id is its id, with base, growth and Max Rank read from config ("runes").
     * A missing number logs an error and the Rune is not registered.
     */
    private static void dropRune(RuneContext ctx, String id, Rarity rarity, Set<Fit> fits) {
        var config = ctx.config();
        var base = config.runeNumber(id, "base");
        var perRank = config.runeNumber(id, "per-rank");
        var maxRank = config.runeNumber(id, "max-rank");
        if (base.isEmpty() || perRank.isEmpty() || maxRank.isEmpty()) {
            ctx.logger().severe("Config 'runes." + id + "' needs base, per-rank and max-rank numbers. Rune '" + id
                    + "' is not registered.");
            return;
        }
        Effect<RuneBlockBreakEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(id, EffectTriggers.BLOCK_BREAK)
                        // The drops are changed by gameplay.drops; this Effect only names the value.
                        .does(context -> {}));
        int max = (int) Math.max(1, maxRank.getAsDouble());
        ctx.get(RuneRegistry.class)
                .register(new Rune(
                        new RuneItem(id, rarity, Material.PAPER, "test_rune"),
                        effect.with(base.getAsDouble(), perRank.getAsDouble(), max),
                        fits));
    }
}
