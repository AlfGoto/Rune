package com.alfgoto.rune.gameplay.traits;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.config.TraitConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.effects.ScaleRegistry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.BlockXpRules;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The Scales of the Copper and Momentum Traits ({@code copper_stage}, {@code momentum}), the wax recipe and the
 * listener that feeds both. Content builds Trait bindings on these Scales. Block Tracking clears a block's tags
 * when it breaks, so this depends on it (and so on Triggers) to read the tags first.
 */
@AutoService(RuneModule.class)
public final class TraitsModule implements RuneModule {

    private JavaPlugin plugin;
    private NamespacedKey waxKey;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, EffectsModule.class, JobsModule.class, BlockTrackingModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        plugin = ctx.plugin();
        ItemRegistry items = ctx.get(ItemRegistry.class);
        TraitConfig config = ctx.config().traits();
        CopperOxidation copper = ctx.provide(CopperOxidation.class, new CopperOxidation(plugin, items, config));
        Momentum momentum = ctx.provide(Momentum.class, new Momentum(config));
        ScaleRegistry scales = ctx.get(ScaleRegistry.class);
        scales.register(copper.scale());
        scales.register(momentum.scale());

        ctx.listen(new TraitListener(
                items, copper, momentum, ctx.get(BlockXpRules.class), ctx.get(BlockTracker.class)));
        waxKey = new NamespacedKey(plugin, WaxListener.RECIPE_KEY);
        plugin.getServer().addRecipe(WaxListener.recipe(waxKey));
        ctx.listen(new WaxListener(waxKey, copper));
    }

    @Override
    public void disable() {
        if (plugin != null && waxKey != null) {
            plugin.getServer().removeRecipe(waxKey);
        }
    }
}
