package com.alfgoto.rune.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class RuneCommandTest {

    private ServerMock server;
    private RunePlugin plugin;
    private RuneCommand command;
    private PlayerMock admin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        command = new RuneCommand(List.of(new VersionSubcommand("1.2.3"), new OtherSubcommand()));
        admin = server.addPlayer();
        admin.addAttachment(plugin, RuneCommand.PERMISSION, true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static TranslatableComponent nextMessage(PlayerMock player) {
        Component message = player.nextComponentMessage();
        assertNotNull(message);
        return (TranslatableComponent) message;
    }

    @Test
    void versionPrintsPluginVersion() {
        command.onCommand(admin, null, "rune", new String[] {"version"});
        TranslatableComponent message = nextMessage(admin);
        assertEquals("rune.command.version", message.key());
        assertEquals(Component.text("1.2.3"), message.arguments().get(0).asComponent());
    }

    @Test
    void unknownSubcommandShowsUsage() {
        command.onCommand(admin, null, "rune", new String[] {"nope"});
        assertEquals("rune.command.usage", nextMessage(admin).key());
    }

    @Test
    void withoutPermissionIsRefused() {
        PlayerMock player = server.addPlayer();
        command.onCommand(player, null, "rune", new String[] {"version"});
        assertEquals("rune.command.no_permission", nextMessage(player).key());
    }

    @Test
    void tabCompletesSubcommandNames() {
        assertEquals(List.of("other", "version"), command.onTabComplete(admin, null, "rune", new String[] {""}));
        assertEquals(List.of("version"), command.onTabComplete(admin, null, "rune", new String[] {"v"}));
        assertTrue(command.onTabComplete(admin, null, "rune", new String[] {"version", ""}).isEmpty());
    }

    @Test
    void pluginRegistersRuneCommand() {
        assertNotNull(plugin.getCommand("rune"));
        assertNotNull(plugin.getCommand("rune").getTabCompleter());
    }

    private static final class OtherSubcommand implements Subcommand {
        @Override
        public String name() {
            return "other";
        }

        @Override
        public void execute(CommandSender sender, List<String> args) {}
    }
}
