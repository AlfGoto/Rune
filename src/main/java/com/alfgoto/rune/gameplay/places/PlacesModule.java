package com.alfgoto.rune.gameplay.places;

import com.alfgoto.rune.command.WhereSubcommand;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Places framework: an empty PlaceRegistry, the detector, its cache cleanup and /rune where. */
@AutoService(RuneModule.class)
public final class PlacesModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        PlaceRegistry registry = ctx.provide(PlaceRegistry.class, new PlaceRegistry());
        PlaceDetector detector = ctx.provide(PlaceDetector.class, new PlaceDetector(registry, new BukkitWorldProbe()));
        ctx.listen(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                detector.forget(event.getPlayer().getUniqueId());
            }
        });
        ctx.subcommand(new WhereSubcommand(detector));
    }
}
