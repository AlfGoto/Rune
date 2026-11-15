package com.alfgoto.rune.content.rewards;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.jobs.AccountXpReward;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobXpReward;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.gameplay.quests.QuestBook;
import com.alfgoto.rune.gameplay.quests.QuestsModule;
import com.alfgoto.rune.gameplay.quests.RerollTokenReward;
import com.alfgoto.rune.gameplay.rewards.LootTable;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.gameplay.rewards.RewardsModule;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RuneReward;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Registers the shared {@code cache} loot table (docs/rewards.md). Base weights live here, tuning numbers in config. */
@AutoService(RuneModule.class)
public final class CoreLootModule implements RuneModule {

    public static final String CACHE = "cache";

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RewardsModule.class, RunesModule.class, JobsModule.class, QuestsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RuneRegistry runes = ctx.get(RuneRegistry.class);
        JobProgress progress = ctx.get(JobProgress.class);
        LootTable cache = LootTable.builder(CACHE)
                .add(new RuneReward(runes, ctx.config(), Rarity.COMMON), 1000, Rarity.COMMON)
                .add(new RuneReward(runes, ctx.config(), Rarity.UNCOMMON), 400, Rarity.UNCOMMON)
                .add(new RuneReward(runes, ctx.config(), Rarity.RARE), 150, Rarity.RARE)
                .add(new RuneReward(runes, ctx.config(), Rarity.EPIC), 40, Rarity.EPIC)
                .add(new RuneReward(runes, ctx.config(), Rarity.LEGENDARY), 5, Rarity.LEGENDARY)
                .add(new JobXpReward(ctx.get(JobRegistry.class), progress, 100), 150, Rarity.COMMON)
                .add(new AccountXpReward(progress, 100), 100, Rarity.COMMON)
                .add(new RerollTokenReward(ctx.get(QuestBook.class).rerollTokens(), 1), 60, Rarity.UNCOMMON)
                .build();
        ctx.get(LootTableRegistry.class).register(cache);
    }
}
