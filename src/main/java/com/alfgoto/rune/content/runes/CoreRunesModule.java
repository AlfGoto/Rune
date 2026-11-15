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
    }
}
