package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.runes.SpeedBonuses;
import com.alfgoto.rune.gameplay.runes.SpeedModule;
import com.alfgoto.rune.gameplay.woodcutting.AxeTraits;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Says when the Acacia Axe speed Trait applies: faster in open sunlight, slower at night and in rain. No Rune is
 * created: the effect only exists as a Trait binding.
 */
@AutoService(RuneModule.class)
public final class AxeRulesModule implements RuneModule {

    /** Sky light of a block that sees the sky. */
    private static final int FULL_SKY_LIGHT = 15;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(SpeedModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        SpeedBonuses speed = ctx.get(SpeedBonuses.class);
        speed.registerByValue(AxeTraits.SUN_RAIN_SPEED, (player, block, percent) -> {
            Location at = player.getLocation();
            World world = at.getWorld();
            // Only the Overworld has a day, a night and rain.
            if (world == null || world.getEnvironment() != World.Environment.NORMAL) {
                return 0;
            }
            boolean night = !world.isDayTime();
            boolean openSky = at.getBlock().getLightFromSky() >= FULL_SKY_LIGHT;
            // Rain only counts when it can fall on the player (open sky).
            boolean raining = world.hasStorm() && openSky;
            return AxeTraits.sunRainSpeed(night, raining, openSky, percent);
        });
    }
}
