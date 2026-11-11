package com.alfgoto.rune.gameplay.places;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class WhereSubcommandTest {

    private ServerMock server;
    private PlayerMock player;
    private String biome;
    private WhereSubcommand where;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        where = new WhereSubcommand(new PlaceDetector(core(), new WorldProbe() {
            @Override
            public String biomeAt(Location location) {
                return biome;
            }

            @Override
            public Set<String> structuresAt(Location location) {
                return Set.of("minecraft:mineshaft");
            }
        }));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static PlaceRegistry core() {
        PlaceRegistry registry = new PlaceRegistry();
        registry.register(Place.biome("lush_caves", "minecraft:lush_caves"));
        registry.register(Place.biome("taiga", "minecraft:taiga", "minecraft:snowy_taiga"));
        registry.register(Place.structure("mineshaft", "minecraft:mineshaft", "minecraft:mineshaft_mesa"));
        return registry;
    }

    private TranslatableComponent next() {
        Component message = player.nextComponentMessage();
        assertNotNull(message);
        return (TranslatableComponent) message;
    }

    @Test
    void listsPlaces() {
        biome = "minecraft:lush_caves";
        where.execute(player, List.of());
        assertEquals("rune.command.where.header", next().key());
        assertNotNull(player.nextComponentMessage());
        assertNotNull(player.nextComponentMessage());
    }

    @Test
    void saysSoWhenInNoPlace() {
        biome = "minecraft:plains";
        player = server.addPlayer();
        where = new WhereSubcommand(new PlaceDetector(core(), new WorldProbe() {
            @Override
            public String biomeAt(Location location) {
                return biome;
            }

            @Override
            public Set<String> structuresAt(Location location) {
                return Set.of();
            }
        }));
        where.execute(player, List.of());
        assertEquals("rune.command.where.none", next().key());
    }
}
