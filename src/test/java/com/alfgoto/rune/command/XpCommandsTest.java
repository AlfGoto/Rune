package com.alfgoto.rune.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.storage.PdcPlayerStorage;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class XpCommandsTest {

    private ServerMock server;
    private PlayerMock player;
    private JobProgress progress;
    private XpSubcommand xp;
    private LevelSubcommand level;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        progress = new JobProgress(new PdcPlayerStorage(plugin), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        // Registry first, then content fills it, like the modules do at startup.
        JobRegistry registry = new JobRegistry();
        CoreJobs.registerAll(registry);
        var jobs = registry.registry();
        xp = new XpSubcommand(jobs, progress);
        level = new LevelSubcommand(jobs, progress);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static String nextKey(PlayerMock p) {
        Component message = p.nextComponentMessage();
        assertNotNull(message);
        return ((TranslatableComponent) message).key();
    }

    @Test
    void addChangesJobXpAndAccountXp() {
        xp.execute(player, List.of("add", "woodcutting", "100"));
        assertEquals("rune.command.xp.changed", nextKey(player));
        assertEquals(100, progress.jobXp(player.getUniqueId(), CoreJobs.WOODCUTTING), 1e-9);
        assertEquals(80, progress.accountXp(player.getUniqueId()), 1e-9);
    }

    @Test
    void setChangesJobXp() {
        xp.execute(player, List.of("set", "mining", "70"));
        assertEquals(70, progress.jobXp(player.getUniqueId(), CoreJobs.MINING), 1e-9);
        assertEquals(2, progress.jobLevel(player.getUniqueId(), CoreJobs.MINING));
    }

    @Test
    void levelSetChangesJobLevel() {
        level.execute(player, List.of("set", "mining", "5"));
        assertEquals("rune.command.level.changed", nextKey(player));
        assertEquals(5, progress.jobLevel(player.getUniqueId(), CoreJobs.MINING));
    }

    @Test
    void showPrintsHeaderJobsAndAccount() {
        xp.execute(player, List.of());
        assertEquals("rune.command.xp.header", nextKey(player));
        assertEquals("rune.command.xp.job", nextKey(player));
        assertEquals("rune.command.xp.job", nextKey(player));
        assertEquals("rune.command.xp.account", nextKey(player));
    }

    @Test
    void showWorksForAnotherPlayer() {
        PlayerMock other = server.addPlayer();
        xp.execute(player, List.of(other.getName()));
        assertEquals("rune.command.xp.header", nextKey(player));
    }

    @Test
    void badInputGivesErrors() {
        xp.execute(player, List.of("add", "nope", "1"));
        assertEquals("rune.command.unknown_job", nextKey(player));
        xp.execute(player, List.of("add", "mining", "abc"));
        assertEquals("rune.command.invalid_number", nextKey(player));
        xp.execute(player, List.of("add", "mining", "-5"));
        assertEquals("rune.command.invalid_number", nextKey(player));
        level.execute(player, List.of("set", "mining", "0"));
        assertEquals("rune.command.invalid_number", nextKey(player));
        level.execute(player, List.of("nope"));
        assertEquals("rune.command.level.usage", nextKey(player));
        xp.execute(player, List.of("nobody"));
        assertEquals("rune.command.player_not_found", nextKey(player));
    }

    @Test
    void tabCompletes() {
        assertEquals(List.of("mining"), xp.complete(player, List.of("set", "m")));
        assertEquals(List.of("set"), level.complete(player, List.of("s")));
        assertEquals(List.of("woodcutting"), level.complete(player, List.of("set", "w")));
    }
}
