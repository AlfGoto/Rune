package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.sanctuary.Sanctuaries;
import com.alfgoto.rune.gameplay.sanctuary.SanctuaryModule;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;

/**
 * Sanctuary: &lt;biome&gt; (docs/jobs/woodcutting.md): a Rare passive Rune for the player and armor, one Rune per
 * biome Place. Numbers (value per Rank, Max Rank) are in config.yml under {@code runes.sanctuary}; the effects
 * run in gameplay.sanctuary.
 */
@AutoService(RuneModule.class)
public final class SanctuaryRunesModule implements RuneModule {

    /** The biome Places that exist today (see CorePlaces). More come with their Places. */
    static final List<String> BIOMES = List.of("lush_caves", "taiga");

    /** Biomes that already have a real Rune texture (tools/texture-gen/v3/runes.py, VARIANTS). */
    private static final Set<String> TEXTURED = Set.of("taiga", "lush_caves");

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, EffectsModule.class, SanctuaryModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        var config = ctx.config();
        var base = config.runeNumber("sanctuary", "base");
        var perRank = config.runeNumber("sanctuary", "per-rank");
        var maxRank = config.runeNumber("sanctuary", "max-rank");
        if (base.isEmpty() || perRank.isEmpty() || maxRank.isEmpty()) {
            ctx.logger().severe("Config 'runes.sanctuary' needs base, per-rank and max-rank numbers. "
                    + "Sanctuary Runes are not registered.");
            return;
        }
        RuneRegistry runes = ctx.get(RuneRegistry.class);
        EffectRegistry effects = ctx.get(EffectRegistry.class);
        Sanctuaries sanctuaries = ctx.get(Sanctuaries.class);
        for (String biome : BIOMES) {
            String id = "sanctuary_" + biome;
            sanctuaries.register(id, biome);
            // The potion effects are given by gameplay.sanctuary, so the Effect itself does nothing.
            Effect<RuneBlockBreakEvent> effect =
                    effects.register(Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {}));
            // Real texture when one exists, else the test model (see docs/design-decisions.md, Textures).
            String model = TEXTURED.contains(biome) ? id : "test_rune";
            runes.register(new Rune(
                    new RuneItem(id, Rarity.RARE, Material.PAPER, model),
                    effect.with(base.getAsDouble(), perRank.getAsDouble(), (int) Math.max(1, maxRank.getAsDouble())),
                    Set.of(Fit.PLAYER)));
        }
    }
}
