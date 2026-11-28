package com.alfgoto.rune.gameplay.structureloot;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.blueprints.Blueprints;
import com.alfgoto.rune.gameplay.blueprints.BlueprintsModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Structure loot: Relics and Blueprints added to structure chests by loot table key, for players who meet the
 * Job Level gate, plus {@code /rune debug loot-chest <loot_table>} to test it. The items come from content modules.
 */
@AutoService(RuneModule.class)
public final class StructureLootModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, JobsModule.class, BlueprintsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        StructureLoot structureLoot = ctx.provide(
                StructureLoot.class,
                new StructureLoot(
                        ctx.config().structureLoot(),
                        ctx.get(JobRegistry.class),
                        ctx.get(JobProgress.class),
                        ctx.get(ItemRegistry.class),
                        ctx.get(Blueprints.class)));
        ctx.listen(new StructureLootListener(structureLoot));
        ctx.debug(new ChestLootDebug(structureLoot, ThreadLocalRandom.current()));
    }
}
