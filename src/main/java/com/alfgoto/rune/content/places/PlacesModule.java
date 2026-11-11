package com.alfgoto.rune.content.places;

import com.alfgoto.rune.command.WhereSubcommand;
import com.alfgoto.rune.gameplay.places.BukkitWorldProbe;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Wires Places: the detector, its cache cleanup and {@code /rune where}. */
@AutoService(RuneModule.class)
public final class PlacesModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        PlaceDetector detector =
                ctx.provide(PlaceDetector.class, new PlaceDetector(CorePlaces.registry(), new BukkitWorldProbe()));
        ctx.listen(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                detector.forget(event.getPlayer().getUniqueId());
            }
        });
        ctx.subcommand(new WhereSubcommand(detector));
    }
}
