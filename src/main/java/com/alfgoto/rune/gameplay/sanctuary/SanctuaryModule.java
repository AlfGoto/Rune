package com.alfgoto.rune.gameplay.sanctuary;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlacesModule;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;

/**
 * Sanctuary framework: an empty {@link Sanctuaries} (content fills it) and the task that applies the effects.
 * Numbers come from {@code runes.sanctuary} in config.yml.
 */
@AutoService(RuneModule.class)
public final class SanctuaryModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, PlacesModule.class, ItemsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        var config = ctx.config();
        var resistanceFrom = config.runeNumber("sanctuary", "resistance-from");
        var regeneration2From = config.runeNumber("sanctuary", "regeneration-2-from");
        var seconds = config.runeNumber("sanctuary", "effect-seconds");
        if (resistanceFrom.isEmpty() || regeneration2From.isEmpty() || seconds.isEmpty()) {
            ctx.logger()
                    .severe("Config 'runes.sanctuary' needs resistance-from, regeneration-2-from and "
                            + "effect-seconds numbers. Sanctuary effects are off.");
            // Still provided (never reached) so the Sanctuary Runes module can load.
            ctx.provide(Sanctuaries.class, new Sanctuaries(Double.MAX_VALUE, Double.MAX_VALUE));
            return;
        }
        Sanctuaries sanctuaries = ctx.provide(
                Sanctuaries.class, new Sanctuaries(resistanceFrom.getAsDouble(), regeneration2From.getAsDouble()));
        SanctuaryTask task = new SanctuaryTask(
                ctx.plugin().getServer(),
                sanctuaries,
                ctx.get(PlayerLoadout.class),
                ctx.get(ToolLoadout.class),
                ctx.get(PlaceDetector.class),
                (int) Math.max(20, Math.round(seconds.getAsDouble() * 20)));
        ctx.plugin()
                .getServer()
                .getScheduler()
                .runTaskTimer(ctx.plugin(), task, SanctuaryTask.PERIOD, SanctuaryTask.PERIOD);
    }
}
