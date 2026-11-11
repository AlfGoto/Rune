package com.alfgoto.rune.content.places;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;
import com.alfgoto.rune.gameplay.places.PlacesModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Registers the core Places into the PlaceRegistry. */
@AutoService(RuneModule.class)
public final class CorePlacesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(PlacesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        CorePlaces.registerAll(ctx.get(PlaceRegistry.class));
    }
}
