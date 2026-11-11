package com.alfgoto.rune.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.alfgoto.rune.content.places.CorePlaces;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlaceRegistry;
import com.alfgoto.rune.gameplay.places.WorldProbe;
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
        CorePlaces.registerAll(registry);
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
