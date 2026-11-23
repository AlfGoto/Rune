package com.alfgoto.rune.visuals.reveal;

import com.alfgoto.rune.content.runes.CoreRunesModule;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Wires Ore Sense: breaking stone can outline nearby ores. Radius and time are in config ({@code runes.ore_sense}). */
@AutoService(RuneModule.class)
public final class OreSenseModule implements RuneModule {

    static final int DEFAULT_RADIUS = 8;
    static final int DEFAULT_SECONDS = 5;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, ItemsModule.class, BlockRevealModule.class, CoreRunesModule.class,
                BlockTrackingModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        int radius = whole(ctx, "radius", DEFAULT_RADIUS, 32);
        int seconds = whole(ctx, "seconds", DEFAULT_SECONDS, 600);
        ctx.listen(new OreSenseListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                ctx.get(BlockReveal.class),
                ctx.get(BlockTracker.class),
                () -> ThreadLocalRandom.current().nextDouble(),
                radius,
                seconds));
    }

    private static int whole(RuneContext ctx, String key, int fallback, int max) {
        var value = ctx.config().runeNumber("ore_sense", key);
        if (value.isEmpty() || value.getAsDouble() < 1 || value.getAsDouble() > max
                || value.getAsDouble() != Math.rint(value.getAsDouble())) {
            ctx.logger()
                    .severe("Config 'runes.ore_sense." + key + "' must be a whole number from 1 to " + max
                            + ". Using default " + fallback + ".");
            return fallback;
        }
        return (int) value.getAsDouble();
    }
}
