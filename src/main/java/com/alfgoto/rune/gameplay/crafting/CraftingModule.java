package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.entity.Player;

/** Crafting framework: an empty RecipeBook, locked recipes and recipe book sync. Content adds recipes. */
@AutoService(RuneModule.class)
public final class CraftingModule implements RuneModule {

    private RecipeBook book;

    @Override
    public void enable(RuneContext ctx) {
        JobProgress progress = ctx.get(JobProgress.class);
        ItemRegistry items = ctx.get(ItemRegistry.class);
        RecipeBook recipeBook = ctx.provide(RecipeBook.class, new RecipeBook(ctx.plugin(), items, progress));
        book = recipeBook;
        ctx.listen(new CraftingListener(recipeBook, items));
        // Unlock on level-up, and lock again when a level goes down.
        progress.addLevelListener((uuid, job, before, after) -> {
            Player player = ctx.plugin().getServer().getPlayer(uuid);
            if (player != null) {
                recipeBook.sync(player);
            }
        });
    }

    @Override
    public void disable() {
        if (book != null) {
            book.unregisterAll();
        }
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, JobsModule.class);
    }
}
