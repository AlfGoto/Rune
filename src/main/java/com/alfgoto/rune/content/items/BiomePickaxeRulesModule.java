package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlacesModule;
import com.alfgoto.rune.gameplay.runes.HeldTraits;
import com.alfgoto.rune.gameplay.runes.HeldTraitsModule;
import com.alfgoto.rune.gameplay.runes.SpeedBonuses;
import com.alfgoto.rune.gameplay.runes.SpeedModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/**
 * Says where the biome pickaxe Traits apply: Affinity speed in Nether, Mountains and Ocean, and the Emerald
 * move speed in Mountains. No Rune is created: these effects only exist as Trait bindings.
 */
@AutoService(RuneModule.class)
public final class BiomePickaxeRulesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(SpeedModule.class, HeldTraitsModule.class, PlacesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        SpeedBonuses speed = ctx.get(SpeedBonuses.class);
        HeldTraits held = ctx.get(HeldTraits.class);
        PlaceDetector detector = ctx.get(PlaceDetector.class);

        speed.register(BiomePickaxeTraits.AFFINITY_NETHER, (player, block) -> detector.isIn(player, "nether"));
        speed.register(BiomePickaxeTraits.AFFINITY_MOUNTAINS, (player, block) -> detector.isIn(player, "mountains"));
        speed.register(BiomePickaxeTraits.AFFINITY_OCEAN, (player, block) -> detector.isIn(player, "ocean"));
        held.registerMoveSpeed(BiomePickaxeTraits.MOUNTAIN_MOVE_SPEED, player -> detector.isIn(player, "mountains"));
    }
}
