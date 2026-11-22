package com.alfgoto.rune.visuals.glow;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.runes.HeldTraits;
import com.alfgoto.rune.gameplay.runes.HeldTraitsModule;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Wires the glow Light block: checks every 2 ticks, clears on quit and on disable. */
@AutoService(RuneModule.class)
public final class GlowLightModule implements RuneModule {

    private static final long PERIOD_TICKS = 2;

    private GlowLight light;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(HeldTraitsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        light = new GlowLight(ctx.plugin(), ctx.get(HeldTraits.class));
        ctx.listen(new QuitListener(light));
        ctx.plugin().getServer().getScheduler().runTaskTimer(ctx.plugin(), light, PERIOD_TICKS, PERIOD_TICKS);
    }

    @Override
    public void disable() {
        if (light != null) {
            light.clearAll();
        }
    }

    private record QuitListener(GlowLight light) implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            light.clear(event.getPlayer().getUniqueId());
        }
    }
}
