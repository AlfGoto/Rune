package com.alfgoto.rune.gameplay.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingListener;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.blocktracking.PdcBlockTracker;
import com.alfgoto.rune.foundations.module.ModuleLoader;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.storage.PlayerStorage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class BlockActionListenerTest {

    private final Job mining = Job.builder("mining").build();
    private final Job woodcutting = Job.builder("woodcutting").build();

    private ServerMock server;
    private World world;
    private PlayerMock player;
    private BlockTracker tracker;
    private JobProgress progress;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("w");
        player = server.addPlayer();
        tracker = new PdcBlockTracker("rune");
        progress = new JobProgress(new MemoryStorage(), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));

        BlockActions actions = new BlockActions();
        actions.register(Material.DIAMOND_ORE, new BlockAction(mining, 30));
        actions.register(Material.OAK_LOG, new BlockAction(woodcutting, 3));
        actions.register(Material.OBSIDIAN, new BlockAction(mining, 6));
        actions.register(Material.COBBLESTONE, new BlockAction(mining, 1));

        // Same order the module loader gives: Triggers before Block Tracking (see BlockTrackingModule).
        List<RuneModule> order = ModuleLoader.sort(List.of(new BlockTrackingModule(), new TriggersModule()));
        assertEquals(TriggersModule.class, order.get(0).getClass());
        Plugin plugin = MockBukkit.createMockPlugin();
        server.getPluginManager().registerEvents(new Triggers(server), plugin);
        server.getPluginManager().registerEvents(new BlockTrackingListener(tracker), plugin);
        server.getPluginManager()
                .registerEvents(
                        new BlockActionListener(actions, new BlockXpRules(0.25), tracker, progress), plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Block block(Material type) {
        Block block = world.getBlockAt(1, 64, 1);
        block.setType(type);
        return block;
    }

    private void breakBlock(Block block) {
        server.getPluginManager().callEvent(new BlockBreakEvent(block, player));
    }

    @Test
    void breakingANaturalBlockGrantsItsXpToItsJob() {
        breakBlock(block(Material.DIAMOND_ORE));
        assertEquals(30, progress.jobXp(player.getUniqueId(), mining), 1e-9);
        assertEquals(0, progress.jobXp(player.getUniqueId(), woodcutting), 1e-9);
    }

    @Test
    void blockWithoutAnActionGivesNothing() {
        breakBlock(block(Material.DIRT));
        assertEquals(0, progress.jobXp(player.getUniqueId(), mining), 1e-9);
    }

    @Test
    void placedBlockGivesNothing() {
        Block block = block(Material.DIAMOND_ORE);
        tracker.add(block, BlockTag.PLACED);
        breakBlock(block);
        assertEquals(0, progress.jobXp(player.getUniqueId(), mining), 1e-9);
    }

    @Test
    void fluidMadeBlockGivesNothingButObsidianKeepsItsXp() {
        Block cobble = block(Material.COBBLESTONE);
        tracker.add(cobble, BlockTag.FLUID);
        breakBlock(cobble);
        assertEquals(0, progress.jobXp(player.getUniqueId(), mining), 1e-9);

        Block obsidian = block(Material.OBSIDIAN);
        tracker.add(obsidian, BlockTag.FLUID);
        breakBlock(obsidian);
        assertEquals(6, progress.jobXp(player.getUniqueId(), mining), 1e-9);
    }

    @Test
    void boneMealGrownBlockGivesAQuarter() {
        Block block = block(Material.OAK_LOG);
        tracker.add(block, BlockTag.GROWN_BONEMEAL);
        breakBlock(block);
        assertEquals(0.75, progress.jobXp(player.getUniqueId(), woodcutting), 1e-9);
    }

    @Test
    void naturallyGrownBlockGivesFullXp() {
        Block block = block(Material.OAK_LOG);
        tracker.add(block, BlockTag.GROWN_NATURAL);
        breakBlock(block);
        assertEquals(3, progress.jobXp(player.getUniqueId(), woodcutting), 1e-9);
    }

    @Test
    void tagsAreStillClearedAfterTheBreak() {
        Block block = block(Material.DIAMOND_ORE);
        tracker.add(block, BlockTag.PLACED);
        breakBlock(block);
        assertTrue(tracker.tags(block).isEmpty());
    }

    @Test
    void aBlockHasOneJobOnly() {
        BlockActions actions = new BlockActions();
        actions.register(Material.OAK_LOG, new BlockAction(woodcutting, 3));
        assertThrows(
                IllegalStateException.class,
                () -> actions.register(Material.OAK_LOG, new BlockAction(mining, 3)));
    }

    private static final class MemoryStorage implements PlayerStorage {
        private final Map<String, String> data = new HashMap<>();

        @Override
        public Optional<String> get(UUID player, String key) {
            return Optional.ofNullable(data.get(player + key));
        }

        @Override
        public void set(UUID player, String key, String value) {
            data.put(player + key, value);
        }

        @Override
        public void remove(UUID player, String key) {
            data.remove(player + key);
        }
    }
}
