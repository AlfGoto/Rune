package com.alfgoto.rune.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.triggers.EventDebug;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.TranslatableComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class DebugSubcommandTest {

    private PlayerMock admin;
    private EventDebug debug;
    private DebugSubcommand command;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        admin = server.addPlayer();
        debug = new EventDebug(Logger.getLogger("test"));
        command = new DebugSubcommand(debug);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void eventsToggles() {
        command.execute(admin, List.of("events"));
        assertTrue(debug.enabled());
        assertEquals(
                "rune.command.debug.events.on",
                ((TranslatableComponent) admin.nextComponentMessage()).key());
        command.execute(admin, List.of("events"));
        assertFalse(debug.enabled());
    }

    @Test
    void badArgsShowUsage() {
        command.execute(admin, List.of());
        assertFalse(debug.enabled());
        assertEquals(
                "rune.command.debug.usage",
                ((TranslatableComponent) admin.nextComponentMessage()).key());
    }

    @Test
    void completesEvents() {
        assertEquals(List.of("events"), command.complete(admin, List.of("e")));
    }
}
