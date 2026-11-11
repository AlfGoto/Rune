package com.alfgoto.rune.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class PdcPlayerStorageTest {

    private ServerMock server;
    private PlayerStorage storage;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        storage = new PdcPlayerStorage(MockBukkit.load(RunePlugin.class));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void missingKeyIsEmpty() {
        PlayerMock player = server.addPlayer();
        assertTrue(storage.get(player.getUniqueId(), "job/mining/xp").isEmpty());
    }

    @Test
    void setThenGet() {
        PlayerMock player = server.addPlayer();
        storage.set(player.getUniqueId(), "quest/active", "a,b,c");
        assertEquals("a,b,c", storage.get(player.getUniqueId(), "quest/active").orElseThrow());
    }

    @Test
    void removeDeletesValue() {
        PlayerMock player = server.addPlayer();
        storage.set(player.getUniqueId(), "k", "v");
        storage.remove(player.getUniqueId(), "k");
        assertTrue(storage.get(player.getUniqueId(), "k").isEmpty());
    }

    @Test
    void dataIsPerPlayer() {
        PlayerMock a = server.addPlayer();
        PlayerMock b = server.addPlayer();
        storage.set(a.getUniqueId(), "k", "a");
        assertTrue(storage.get(b.getUniqueId(), "k").isEmpty());
    }

    @Test
    void offlinePlayerThrows() {
        assertThrows(IllegalStateException.class, () -> storage.get(UUID.randomUUID(), "k"));
    }
}
