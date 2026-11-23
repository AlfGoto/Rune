package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.config.RhythmConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.trees.TreesModule;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.effects.ScaleRegistry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Woodcutting Runes that act on leaves and movement (Leaf Fall, Orchard, Canopy Stride, Replant) and Rhythm, the
 * Woodcutting armor mechanic: charges from Felled Trees, the {@code rhythm} Scale and the armor effects.
 */
@AutoService(RuneModule.class)
public final class WoodcuttingModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class, EffectsModule.class, TreesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        ToolLoadout loadout = ctx.get(ToolLoadout.class);
        ctx.listen(new LeafListener(items, loadout, () -> ThreadLocalRandom.current().nextDouble()));
        ctx.listen(new CanopyListener(ctx.plugin(), items, loadout));
        ctx.listen(new ReplantListener(ctx.plugin(), items, loadout));

        RhythmConfig config = ctx.config().rhythm();
        Rhythm rhythm = ctx.provide(
                Rhythm.class, new Rhythm(config.maxCharges(), config.resetSeconds() * 1000L, System::currentTimeMillis));
        ctx.get(ScaleRegistry.class).register(new RhythmScale(rhythm));
        ctx.listen(new RhythmListener(ctx.plugin(), rhythm, loadout, config.resetSeconds()));
        ctx.listen(new RhythmArmorListener(ctx.plugin(), loadout));
    }
}
