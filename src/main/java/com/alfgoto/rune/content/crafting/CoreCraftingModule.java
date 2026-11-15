package com.alfgoto.rune.content.crafting;

import com.alfgoto.rune.content.items.CoreItemsModule;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.crafting.CraftingModule;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;

/** Registers the core recipes. For now only a test recipe: 4 cobblestone → Test Item, at Mining level 2. */
@AutoService(RuneModule.class)
public final class CoreCraftingModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(CraftingModule.class, CoreItemsModule.class, CoreJobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RecipeBook book = ctx.get(RecipeBook.class);
        book.register(new CraftingRecipe(
                "test_item",
                ctx.get(JobRegistry.class).registry().get("mining"),
                2,
                "test_item",
                1,
                List.of("CC", "CC"),
                Map.of('C', Ingredient.vanilla(Material.COBBLESTONE))));
    }
}
