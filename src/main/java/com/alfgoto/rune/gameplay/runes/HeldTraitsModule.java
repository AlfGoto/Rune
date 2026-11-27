package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Held-tool effects (glow, move speed, mining penalties): the {@link HeldTraits} service and its timer. */
@AutoService(RuneModule.class)
public final class HeldTraitsModule implements RuneModule {

    /** Checks every 2 ticks (10 times a second). */
    private static final long PERIOD_TICKS = 2;

    private HeldTraits held;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        held = ctx.provide(HeldTraits.class, new HeldTraits(ctx.plugin(), ctx.get(ToolLoadout.class),
                ctx.get(BrokenToolListener.class)::isBroken));
        ctx.listen(held);
        ctx.plugin().getServer().getScheduler().runTaskTimer(ctx.plugin(), held, PERIOD_TICKS, PERIOD_TICKS);
    }

    @Override
    public void disable() {
        if (held != null) {
            held.clear();
        }
    }
}
