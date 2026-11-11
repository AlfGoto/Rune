package com.alfgoto.rune.gameplay.places;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.content.places.CorePlaces;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class PlaceDetectorTest {

    private ServerMock server;
    private PlayerMock player;
    private String biome = "minecraft:plains";
    private Set<String> structures = Set.of();
    private final AtomicInteger lookups = new AtomicInteger();
    private PlaceDetector detector;
    private PlaceRegistry registry;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        registry = new PlaceRegistry();
        CorePlaces.registerAll(registry);
        detector = new PlaceDetector(registry, new WorldProbe() {
            @Override
            public String biomeAt(Location location) {
                lookups.incrementAndGet();
                return biome;
            }

            @Override
            public Set<String> structuresAt(Location location) {
                return structures;
            }
        });
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private List<String> ids() {
        return detector.placesOf(player).stream().map(Place::id).toList();
    }

    @Test
    void plainsIsNoPlace() {
        assertTrue(ids().isEmpty());
    }

    @Test
    void matchesBiome() {
        biome = "minecraft:taiga";
        assertEquals(List.of("taiga"), ids());
    }

    @Test
    void matchesBiomeAndStructureTogether() {
        biome = "minecraft:lush_caves";
        structures = Set.of("minecraft:mineshaft");
        assertEquals(List.of("lush_caves", "mineshaft"), ids());
    }

    @Test
    void detectsPlaceRegisteredAfterDetectorIsCreated() {
        biome = "minecraft:desert";
        assertTrue(ids().isEmpty());
        registry.register(Place.biome("desert", "minecraft:desert"));
        player.teleport(player.getLocation().add(5, 0, 0));
        assertEquals(List.of("desert"), ids());
    }

    @Test
    void cachesWhileOnTheSameBlock() {
        ids();
        ids();
        assertEquals(1, lookups.get());
        player.teleport(player.getLocation().add(5, 0, 0));
        ids();
        assertEquals(2, lookups.get());
    }

    @Test
    void forgetClearsCache() {
        ids();
        detector.forget(player.getUniqueId());
        ids();
        assertEquals(2, lookups.get());
    }
}
