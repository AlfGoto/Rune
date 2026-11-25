package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

/** Crafting framework: an empty RecipeBook, locked recipes and recipe book sync. Content adds recipes. */
@AutoService(RuneModule.class)
public final class CraftingModule implements RuneModule {

    private RecipeBook book;
    private RankUpListener rankUpListener;

    @Override
    public void enable(RuneContext ctx) {
        JobProgress progress = ctx.get(JobProgress.class);
        ItemRegistry items = ctx.get(ItemRegistry.class);
        RecipeBook recipeBook = ctx.provide(RecipeBook.class, new RecipeBook(
                ctx.plugin(), items, progress, ctx.get(BrokenToolListener.class)::isBroken));
        book = recipeBook;
        // Server recipes are added one tick later, once every item redrawer exists (the book shows the Rune name).
        recipeBook.deferRegistration();
        ctx.plugin().getServer().getScheduler().runTask(ctx.plugin(), recipeBook::flush);
        ctx.listen(new CraftingListener(
                recipeBook, items, ctx.get(ToolLoadout.class), ctx.get(RuneRegistry.class)));
        // Rank-up at the smithing table. The hidden recipe is registered one tick later, once every module
        // has registered its Runes.
        RuneRegistry runeRegistry = ctx.get(RuneRegistry.class);
        RankUp rankUp = ctx.provide(RankUp.class, new RankUp(runeRegistry, ctx.config()));
        rankUpListener = new RankUpListener(
                ctx.plugin(),
                rankUp,
                () -> runeRegistry.all().stream().map(rune -> rune.item().material()).collect(Collectors.toSet()));
        ctx.listen(rankUpListener);
        ctx.plugin().getServer().getScheduler().runTask(ctx.plugin(), rankUpListener::registerRecipe);
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
        if (rankUpListener != null) {
            rankUpListener.unregisterRecipe();
        }
        if (book != null) {
            book.unregisterAll();
        }
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, JobsModule.class, RunesModule.class);
    }
}
