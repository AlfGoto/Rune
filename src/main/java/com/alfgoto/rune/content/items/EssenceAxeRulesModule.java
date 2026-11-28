package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.runes.SpeedBonuses;
import com.alfgoto.rune.gameplay.runes.SpeedModule;
import com.alfgoto.rune.gameplay.woodcutting.AxeTraits;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.World;

/**
 * Says when the Pale Oak Axe drawback applies: its mining speed goes down in daylight (day time and open sky).
 * No Rune is created: the effect only exists as a Trait binding with a negative value.
 */
@AutoService(RuneModule.class)
public final class EssenceAxeRulesModule implements RuneModule {

    /** Sky light at the player's head that counts as daylight (15 = open sky). */
    private static final int DAYLIGHT_SKY_LIGHT = 15;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(SpeedModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        SpeedBonuses speed = ctx.get(SpeedBonuses.class);
        speed.register(AxeTraits.DAYLIGHT_SLOWER, (player, block) -> {
            World world = player.getWorld();
            return world.getEnvironment() == World.Environment.NORMAL
                    && world.isDayTime()
                    && player.getEyeLocation().getBlock().getLightFromSky() >= DAYLIGHT_SKY_LIGHT;
        });
    }
}
