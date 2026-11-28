package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rewards engine: an empty LootTableRegistry, a LootRoller and {@code /rune loot}. It knows no reward
 * kind: Runes, items, Jobs and Quests bring their own {@link Reward}s, and content fills the tables.
 */
@AutoService(RuneModule.class)
public final class RewardsModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        LootTableRegistry tables = ctx.provide(LootTableRegistry.class, new LootTableRegistry());
        LootRoller roller = ctx.provide(LootRoller.class, new LootRoller(ctx.config()));
        ctx.subcommand(ctx.provide(
                LootSubcommand.class, new LootSubcommand(tables.registry(), roller, ThreadLocalRandom.current())));
    }
}
