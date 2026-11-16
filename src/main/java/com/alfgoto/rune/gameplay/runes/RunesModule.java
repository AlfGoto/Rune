package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.storage.PlayerStorage;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Runes framework: an empty RuneRegistry and {@code /rune give rune}. Content modules fill the registry. */
@AutoService(RuneModule.class)
public final class RunesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, JobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RuneRegistry registry = ctx.provide(
                RuneRegistry.class,
                new RuneRegistry(ctx.plugin(), ctx.get(ItemRegistry.class), new CapacityCost(ctx.config())));
        PlayerCapacity playerCapacity = ctx.provide(PlayerCapacity.class, new PlayerCapacity(ctx.config()));
        PlayerLoadout playerLoadout = ctx.provide(
                PlayerLoadout.class,
                new PlayerLoadout(ctx.get(PlayerStorage.class), registry, playerCapacity, ctx.get(JobProgress.class)));
        ctx.get(GiveSubcommand.class).addVariant(new GiveRuneVariant(registry));
        ToolLoadout loadout =
                ctx.provide(ToolLoadout.class, new ToolLoadout(ctx.plugin(), ctx.get(ItemRegistry.class), registry));
        ctx.debug(LoadoutSubcommands.install(loadout, registry));
        ctx.debug(LoadoutSubcommands.uninstall(loadout));
        ctx.debug(LoadoutSubcommands.installPlayer(playerLoadout, registry));
    }
}
