package com.alfgoto.rune.gameplay.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.storage.PdcPlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class QuestBookTest {

    private ServerMock server;
    private PlayerMock player;
    private PdcPlayerStorage storage;
    private JobProgress progress;
    private QuestTypeRegistry types;
    private QuestBook book;
    private QuestSubcommand command;
    private QuestDebug debug;
    private long day = 0;
    private final QuestType mineTest = new QuestType("minetest", CoreJobs.MINING, 10);
    private final QuestType woodTest = new QuestType("woodtest", CoreJobs.WOODCUTTING, 7);

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        storage = new PdcPlayerStorage(plugin);
        progress = new JobProgress(storage, new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        types = new QuestTypeRegistry();
        book = new QuestBook(
                storage, progress, types.registry(), RuneConfig.defaults(), new Random(3), () -> day);
        command = new QuestSubcommand(book);
        debug = new QuestDebug(types.registry(), book);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void alwaysThreeActiveQuests() {
        types.register(mineTest);
        List<Quest> first = book.active(player.getUniqueId());
        assertEquals(3, first.size());
        assertEquals(first, book.active(player.getUniqueId()), "quests are stored, not redrawn");
    }

    @Test
    void noQuestsWhenNothingIsEligible() {
        assertTrue(book.active(player.getUniqueId()).isEmpty());
    }

    @Test
    void drawsOnlyFromEligibleJobs() {
        Job crafting = Job.builder("crafty").questMinLevel(2).build();
        types.register(new QuestType("craft", crafting, 5));
        assertTrue(book.active(player.getUniqueId()).isEmpty(), "level 1 is below the Job's minimum of 2");
        progress.setJobLevel(player.getUniqueId(), crafting, 2);
        assertEquals(3, book.active(player.getUniqueId()).size());
    }

    @Test
    void targetIsBaseSizeTimesTierMultiplierRoundedUp() {
        assertEquals(10, book.target(mineTest, Rarity.COMMON));
        assertEquals(15, book.target(mineTest, Rarity.UNCOMMON));
        assertEquals(20, book.target(mineTest, Rarity.RARE));
        assertEquals(30, book.target(mineTest, Rarity.EPIC));
        assertEquals(50, book.target(mineTest, Rarity.LEGENDARY));
        assertEquals(11, book.target(woodTest, Rarity.UNCOMMON)); // 7 × 1.5 = 10.5
    }

    @Test
    void newPlayersNeverGetEpicOrLegendary() {
        types.register(mineTest);
        for (int i = 0; i < 200; i++) {
            Quest quest = book.draw(player.getUniqueId());
            assertTrue(quest.tier().ordinal() <= Rarity.RARE.ordinal());
        }
    }

    @Test
    void highLevelPlayersCanGetLegendary() {
        types.register(mineTest);
        progress.setJobLevel(player.getUniqueId(), CoreJobs.MINING, 50);
        boolean legendary = false;
        for (int i = 0; i < 500 && !legendary; i++) {
            legendary = book.draw(player.getUniqueId()).tier() == Rarity.LEGENDARY;
        }
        assertTrue(legendary);
    }

    @Test
    void usesTheLevelOfTheDrawnJob() {
        types.register(mineTest);
        types.register(woodTest);
        progress.setJobLevel(player.getUniqueId(), CoreJobs.MINING, 50); // Woodcutting stays at 1
        for (int i = 0; i < 300; i++) {
            Quest quest = book.draw(player.getUniqueId());
            if (quest.type().job() == CoreJobs.WOODCUTTING) {
                assertTrue(quest.tier().ordinal() <= Rarity.RARE.ordinal());
            }
        }
    }

    @Test
    void setReplacesASlotAndIsStored() {
        types.register(mineTest);
        book.active(player.getUniqueId());
        Quest set = book.set(player.getUniqueId(), 2, mineTest, Rarity.LEGENDARY);
        assertEquals(50, set.target());
        assertEquals(set, book.active(player.getUniqueId()).get(1));
    }

    @Test
    void unknownStoredTypeIsRedrawn() {
        types.register(mineTest);
        book.set(player.getUniqueId(), 1, mineTest, Rarity.COMMON);
        // A second book with an empty registry does not know the type: the slot is drawn again, here to nothing.
        var other = new QuestBook(
                storage,
                progress,
                new QuestTypeRegistry().registry(),
                RuneConfig.defaults(),
                new Random(1),
                () -> day);
        assertTrue(other.active(player.getUniqueId()).isEmpty());
    }

    @Test
    void commandListsThreeQuests() {
        types.register(mineTest);
        command.execute(player, List.of("list"));
        assertEquals("rune.command.quest.header", nextKey());
        for (int i = 0; i < 3; i++) {
            assertEquals("rune.command.quest.entry", nextKey());
        }
    }

    @Test
    void commandSetsAQuest() {
        types.register(mineTest);
        debug.execute(player, List.of("set", "3", "minetest", "epic"));
        assertEquals("rune.command.quest.changed", nextKey());
        Quest quest = book.active(player.getUniqueId()).get(2);
        assertEquals(mineTest, quest.type());
        assertEquals(Rarity.EPIC, quest.tier());
    }

    @Test
    void commandBadInputGivesErrors() {
        types.register(mineTest);
        debug.execute(player, List.of("set", "4", "minetest", "epic"));
        assertEquals("rune.command.quest.invalid_slot", nextKey());
        debug.execute(player, List.of("set", "1", "nope", "epic"));
        assertEquals("rune.command.quest.unknown_type", nextKey());
        debug.execute(player, List.of("set", "1", "minetest", "mythic"));
        assertEquals("rune.command.quest.unknown_tier", nextKey());
        debug.execute(player, List.of());
        assertEquals("rune.debug.quest.usage", nextKey());
        command.execute(player, List.of("set", "1", "minetest", "epic"));
        assertEquals("rune.command.quest.usage", nextKey());
    }

    @Test
    void tabCompletes() {
        types.register(mineTest);
        assertEquals(List.of("list"), command.complete(player, List.of("l")));
        assertEquals(List.of("minetest"), debug.complete(player, List.of("set", "1", "m")));
        assertEquals(List.of("epic"), debug.complete(player, List.of("set", "1", "minetest", "e")));
    }

    @Test
    void newPlayersStartWithNoRerollToken() {
        assertEquals(0, book.rerollTokens().count(player.getUniqueId()));
    }

    @Test
    void earnsOneRerollTokenPerWorldDay() {
        var id = player.getUniqueId();
        book.rerollTokens().count(id);
        day = 1;
        assertEquals(1, book.rerollTokens().count(id));
        assertEquals(1, book.rerollTokens().count(id), "same day earns nothing more");
        day = 3;
        assertEquals(3, book.rerollTokens().count(id));
    }

    @Test
    void rerollTokensAreCappedAtThree() {
        var id = player.getUniqueId();
        book.rerollTokens().count(id);
        day = 100;
        assertEquals(3, book.rerollTokens().count(id));
        day = 200;
        assertEquals(3, book.rerollTokens().count(id));
    }

    @Test
    void spendingATokenKeepsTheCountRight() {
        var id = player.getUniqueId();
        book.rerollTokens().count(id);
        assertEquals(false, book.rerollTokens().spend(id), "none to spend");
        day = 2;
        assertTrue(book.rerollTokens().spend(id));
        assertEquals(1, book.rerollTokens().count(id));
    }

    @Test
    void rerollReplacesTheSlotAndSpendsAToken() {
        types.register(mineTest);
        var id = player.getUniqueId();
        book.active(id);
        assertTrue(book.reroll(id, 1).isEmpty(), "no token yet");
        day = 1;
        var quest = book.reroll(id, 2);
        assertTrue(quest.isPresent());
        assertEquals(quest.get(), book.active(id).get(1));
        assertEquals(0, book.rerollTokens().count(id));
    }

    @Test
    void rerollCommand() {
        types.register(mineTest);
        command.execute(player, List.of("reroll", "1"));
        assertEquals("rune.command.quest.no_tokens", nextKey());
        day = 1;
        command.execute(player, List.of("reroll", "1"));
        assertEquals("rune.command.quest.rerolled", nextKey());
        command.execute(player, List.of("reroll", "9"));
        assertEquals("rune.command.quest.invalid_slot", nextKey());
        assertEquals(List.of("reroll"), command.complete(player, List.of("r")));
    }

    private String nextKey() {
        Component message = player.nextComponentMessage();
        assertNotNull(message);
        return ((TranslatableComponent) message).key();
    }
}
