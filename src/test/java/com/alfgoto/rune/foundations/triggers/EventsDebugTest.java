package com.alfgoto.rune.foundations.triggers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.TranslatableComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class EventsDebugTest {

    private PlayerMock admin;
    private EventDebug debug;
    private EventsDebug command;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        admin = server.addPlayer();
        debug = new EventDebug(Logger.getLogger("test"));
        command = new EventsDebug(debug);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void nameIsEvents() {
        assertEquals("events", command.name());
    }

    @Test
    void toggles() {
        command.execute(admin, List.of());
        assertTrue(debug.enabled());
        assertEquals("rune.debug.events.on", ((TranslatableComponent) admin.nextComponentMessage()).key());
        command.execute(admin, List.of());
        assertFalse(debug.enabled());
        assertEquals("rune.debug.events.off", ((TranslatableComponent) admin.nextComponentMessage()).key());
    }
}
