package com.alfgoto.rune.gameplay.rewards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class LootCommandTest {

    private ServerMock server;
    private PlayerMock player;
    private int given;
    private String lastJob;
    private LootSubcommand command;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        player.addAttachment(plugin, RuneCommand.PERMISSION, true);
        Reward reward = new Reward() {
            @Override
            public boolean available(RollContext context) {
                return true;
            }

            @Override
            public Drop roll(RollContext context, java.util.random.RandomGenerator random) {
                lastJob = context.jobId();
                return new Drop() {
                    @Override
                    public Component describe() {
                        return Component.text("a prize");
                    }

                    @Override
                    public void give(Player p) {
                        given++;
                    }
                };
            }
        };
        Registry<LootTable> tables = new Registry<>("loot tables");
        tables.register(LootTable.builder("cache").add(reward, 1, Rarity.COMMON).build());
        command = new LootSubcommand(tables, new LootRoller(RuneConfig.defaults()), new Random(1));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private String nextMessage() {
        Component message = player.nextComponentMessage();
        return message == null ? null : PlainTextComponentSerializer.plainText().serialize(message);
    }

    @Test
    void rollGivesAndPrintsEveryDrop() {
        command.execute(player, List.of("roll", "cache", "legendary", "mining"));
        assertEquals(3, given); // a Legendary source rolls 3 times
        assertEquals("mining", lastJob);
        assertTrue(nextMessage().contains("cache"));
        assertEquals("- a prize", nextMessage());
    }

    @Test
    void defaultsToCommonAndNoJob() {
        command.execute(player, List.of("roll", "cache"));
        assertEquals(1, given);
        assertEquals(null, lastJob);
    }

    @Test
    void badArgumentsGiveNothing() {
        command.execute(player, List.of("roll", "nope"));
        command.execute(player, List.of("roll", "cache", "mythic"));
        command.execute(player, List.of());
        assertEquals(0, given);
        assertTrue(nextMessage().contains("nope"));
        assertTrue(nextMessage().contains("mythic"));
        assertTrue(nextMessage().contains("Usage"));
    }

    @Test
    void completesTablesAndRarities() {
        assertEquals(List.of("cache"), command.complete(player, List.of("roll", "c")));
        assertEquals(List.of("epic"), command.complete(player, List.of("roll", "cache", "e")));
    }

    @Test
    void theShippedCacheTableRollsThroughTheRealCommand() {
        server.execute("rune", player, "loot", "roll", "cache", "epic");
        String first = nextMessage();
        assertTrue(first.startsWith("Rolled cache"), first);
    }
}
