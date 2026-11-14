package com.alfgoto.rune.visuals.resourcepack;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PackServingTest {

    @Test
    void serverServesTheZipBytes() throws Exception {
        byte[] zip = ResourcePackBuilder.buildBytes();
        PackServer server = PackServer.start(0, zip);
        try {
            var client = HttpClient.newHttpClient();
            var uri = URI.create("http://localhost:" + server.port() + PackServer.PATH);
            var get = client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, get.statusCode());
            assertArrayEquals(zip, get.body());
            var other = URI.create("http://localhost:" + server.port() + "/nope");
            var missing = client.send(HttpRequest.newBuilder(other).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(404, missing.statusCode());
            var post = client.send(
                    HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(405, post.statusCode());
        } finally {
            server.stop();
        }
    }

    @Test
    void builtInUrlUsesTheJoinedHost() {
        var settings = new PackSettings(8123, null);
        assertEquals(
                "http://play.example.com:8123/rune-pack.zip", PackJoinListener.url(settings, "play.example.com"));
        assertEquals("http://localhost:8123/rune-pack.zip", PackJoinListener.url(settings, null));
        assertEquals("http://[::1]:8123/rune-pack.zip", PackJoinListener.url(settings, "::1"));
    }

    @Test
    void externalUrlWins() {
        var settings = new PackSettings(8123, "https://example.com/pack.zip");
        assertEquals("https://example.com/pack.zip", PackJoinListener.url(settings, "play.example.com"));
    }

    @Test
    void settingsReadDefaultsAndExternalUrl() {
        var empty = new YamlConfiguration();
        empty.set(PackSettings.PORT, 9000);
        empty.set(PackSettings.EXTERNAL_URL, "");
        var s = PackSettings.load(empty, Logger.getAnonymousLogger());
        assertEquals(9000, s.port());
        assertNull(s.externalUrl());
        assertFalse(s.usesExternalUrl());

        var external = new YamlConfiguration();
        external.set(PackSettings.PORT, 9000);
        external.set(PackSettings.EXTERNAL_URL, " https://example.com/p.zip ");
        assertEquals("https://example.com/p.zip", PackSettings.load(external, Logger.getAnonymousLogger()).externalUrl());
    }

    @Test
    void badSettingsFallBack() {
        var bad = new YamlConfiguration();
        bad.set(PackSettings.PORT, 70000);
        bad.set(PackSettings.EXTERNAL_URL, "not a url");
        var s = PackSettings.load(bad, Logger.getAnonymousLogger());
        assertEquals(PackSettings.DEFAULT_PORT, s.port());
        assertFalse(s.usesExternalUrl());
    }

    @Test
    void defaultConfigFileIsValid() throws IOException {
        try (var in = getClass().getResourceAsStream("/config.yml")) {
            var config = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(in));
            var s = PackSettings.load(config, Logger.getAnonymousLogger());
            assertEquals(PackSettings.DEFAULT_PORT, s.port());
            assertTrue(s.externalUrl() == null);
        }
    }
}
