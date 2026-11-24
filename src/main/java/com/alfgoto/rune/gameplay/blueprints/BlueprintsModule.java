package com.alfgoto.rune.gameplay.blueprints;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.crafting.CraftingModule;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.gameplay.runes.IndestructibleItems;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.storage.PlayerStorage;
import com.google.auto.service.AutoService;
import java.util.Set;

/**
 * Blueprints: using a Blueprint unlocks its Legendary recipe, and Milestone Blueprints are given on level-up.
 * The item and the Blueprints themselves are registered by content ({@code CoreBlueprintsModule}).
 */
@AutoService(RuneModule.class)
public final class BlueprintsModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, CraftingModule.class, JobsModule.class, RunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RecipeBook book = ctx.get(RecipeBook.class);
        Blueprints blueprints = ctx.provide(
                Blueprints.class,
                new Blueprints(ctx.plugin(), ctx.get(ItemRegistry.class), book, ctx.get(PlayerStorage.class)));
        book.setBlueprintCheck(blueprints::isUnlocked);
        ctx.get(GiveSubcommand.class).addVariant(new GiveBlueprintVariant(blueprints));
        ctx.get(IndestructibleItems.class).protect(stack -> blueprints.idOf(stack).isPresent());
        ctx.listen(new BlueprintListener(blueprints));
        ctx.get(JobProgress.class).addLevelListener(new BlueprintMilestones(blueprints, ctx.config().blueprints()));
    }
}
