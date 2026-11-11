package com.alfgoto.rune;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.command.CoreCommandsModule;
import com.alfgoto.rune.visuals.resourcepack.ResourcePackModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class RunePluginTest {

    private ServerMock server;
    private RunePlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void pluginEnables() {
        assertTrue(plugin.isEnabled());
    }

    /** Fails if @AutoService stops generating META-INF/services. */
    @Test
    void modulesAreFoundAutomatically() {
        var names = plugin.modules().stream().map(m -> m.getClass()).toList();
        assertTrue(names.contains(CoreCommandsModule.class), names.toString());
        assertTrue(names.contains(ResourcePackModule.class), names.toString());
    }
}
