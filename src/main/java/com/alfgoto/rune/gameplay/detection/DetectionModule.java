package com.alfgoto.rune.gameplay.detection;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Echo ping. Numbers come from config: runes.echo_ping (interval-seconds, radius, seconds). Bone and the
 * Ore Sense Rune use runes.ore_sense, read in visuals.reveal.
 */
@AutoService(RuneModule.class)
public final class DetectionModule implements RuneModule {

    private BukkitTask task;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        DetectionSettings settings = settings(ctx.config());
        DetectionListener listener = new DetectionListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                settings,
                ctx.get(BrokenToolListener.class)::isBroken);
        task = ctx.plugin()
                .getServer()
                .getScheduler()
                .runTaskTimer(
                        ctx.plugin(),
                        () -> ctx.plugin().getServer().getOnlinePlayers().forEach(listener::ping),
                        settings.echoIntervalTicks(),
                        settings.echoIntervalTicks());
    }

    @Override
    public void disable() {
        if (task != null) {
            task.cancel();
        }
    }

    static DetectionSettings settings(RuneConfig config) {
        return new DetectionSettings(
                (int) config.runeNumber("echo_ping", "radius").orElse(12),
                (int) (config.runeNumber("echo_ping", "seconds").orElse(5) * 20),
                (int) (config.runeNumber("echo_ping", "interval-seconds").orElse(10) * 20));
    }
}
