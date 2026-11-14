package com.alfgoto.rune.content.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.command.DebugCommand;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.gameplay.jobs.BlockActions;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class CoreJobsModuleTest {

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void putsMiningAndWoodcuttingInTheSharedRegistry() {
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        RuneContext ctx =
                new RuneContext(plugin, RuneConfig.defaults(), new RuneCommand(List.of()), new DebugCommand());
        JobRegistry jobs = ctx.provide(JobRegistry.class, new JobRegistry());
        BlockActions blockActions = ctx.provide(BlockActions.class, new BlockActions());

        CoreJobsModule module = new CoreJobsModule();
        module.enable(ctx);

        assertEquals(2, jobs.registry().size());
        assertSame(CoreJobs.MINING, jobs.registry().get("mining"));
        assertSame(CoreJobs.WOODCUTTING, jobs.registry().get("woodcutting"));
        assertSame(CoreJobs.MINING, blockActions.get(Material.DIAMOND_ORE).orElseThrow().job());
        assertTrue(module.dependsOn().contains(JobsModule.class));
    }
}
