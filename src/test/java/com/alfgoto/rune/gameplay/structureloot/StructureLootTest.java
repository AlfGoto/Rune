package com.alfgoto.rune.gameplay.structureloot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.StructureLootConfig;
import com.alfgoto.rune.foundations.config.StructureLootConfig.Entry;
import com.alfgoto.rune.gameplay.blueprints.Blueprint;
import com.alfgoto.rune.gameplay.blueprints.Blueprints;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class StructureLootTest {

    private static final String TABLE = "minecraft:chests/ancient_city";

    private final Job mining = Job.builder("mining").build();
    private PlayerMock player;
    private JobProgress progress;
    private Blueprints blueprints;
    private ItemRegistry items;
    private JobRegistry jobs;
    private RunePlugin plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        Map<String, String> data = new HashMap<>();
        PlayerStorage storage = new PlayerStorage() {
            @Override
            public Optional<String> get(UUID player, String key) {
                return Optional.ofNullable(data.get(player + key));
            }

            @Override
            public void set(UUID player, String key, String value) {
                data.put(player + key, value);
            }

            @Override
            public void remove(UUID player, String key) {
                data.remove(player + key);
            }
        };
        progress = new JobProgress(storage, new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        jobs = new JobRegistry();
        jobs.register(mining);
        items = new ItemRegistry(plugin);
        items.register(new RuneItem(Blueprints.ITEM_ID, Rarity.LEGENDARY, Material.NAME_TAG, "test_rune"));
        items.register(new RuneItem("relic", Rarity.LEGENDARY, Material.PAPER, "test_rune"));
        RecipeBook book = new RecipeBook(plugin, items, progress);
        book.register(CraftingRecipe.blueprint(
                "bp", mining, "relic", 1, List.of("SS"), Map.of('S', Ingredient.vanilla(Material.STONE))));
        blueprints = new Blueprints(plugin, items, book, storage);
        blueprints.register(new Blueprint("bp"));
        player = MockBukkit.getMock().addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private StructureLoot loot(Entry... entries) {
        return new StructureLoot(
                new StructureLootConfig(List.of(entries)), jobs, progress, items, blueprints);
    }

    @Test
    void belowTheGateGivesNothing() {
        progress.setJobLevel(player.getUniqueId(), mining, 24);
        StructureLoot loot = loot(new Entry(TABLE, false, "relic", 1.0, "mining", 25));
        assertTrue(loot.roll(player.getUniqueId(), TABLE, new Random(1)).isEmpty());
    }

    @Test
    void atTheGateGivesTheItem() {
        progress.setJobLevel(player.getUniqueId(), mining, 25);
        StructureLoot loot = loot(new Entry(TABLE, false, "relic", 1.0, "mining", 25));
        assertEquals(1, loot.roll(player.getUniqueId(), TABLE, new Random(1)).size());
    }

    @Test
    void chanceZeroNeverGivesAndOtherTablesAreIgnored() {
        progress.setJobLevel(player.getUniqueId(), mining, 40);
        StructureLoot loot = loot(
                new Entry(TABLE, false, "relic", 0.0, "mining", 25),
                new Entry("minecraft:chests/other", false, "relic", 1.0, "mining", 25));
        for (int i = 0; i < 100; i++) {
            assertTrue(loot.roll(player.getUniqueId(), TABLE, new Random(i)).isEmpty());
        }
    }

    @Test
    void chanceIsRespected() {
        progress.setJobLevel(player.getUniqueId(), mining, 40);
        StructureLoot loot = loot(new Entry(TABLE, false, "relic", 0.25, "mining", 25));
        Random random = new Random(7);
        int hits = 0;
        for (int i = 0; i < 4000; i++) {
            hits += loot.roll(player.getUniqueId(), TABLE, random).size();
        }
        assertTrue(hits > 850 && hits < 1150, "hits: " + hits);
    }

    @Test
    void unknownItemUnknownJobAndUsedBlueprintAreSkipped() {
        progress.setJobLevel(player.getUniqueId(), mining, 40);
        StructureLoot loot = loot(
                new Entry(TABLE, false, "not_registered", 1.0, "mining", 1),
                new Entry(TABLE, false, "relic", 1.0, "nope", 1),
                new Entry(TABLE, true, "bp", 1.0, "mining", 20));
        assertEquals(1, loot.roll(player.getUniqueId(), TABLE, new Random(1)).size());
        blueprints.unlock(player, blueprints.find("bp").orElseThrow());
        assertTrue(loot.roll(player.getUniqueId(), TABLE, new Random(1)).isEmpty());
    }

    @Test
    void blueprintResultIsABlueprintItem() {
        progress.setJobLevel(player.getUniqueId(), mining, 20);
        StructureLoot loot = loot(new Entry(TABLE, true, "bp", 1.0, "mining", 20));
        StructureLoot.Result result =
                loot.roll(player.getUniqueId(), TABLE, new Random(1)).get(0);
        assertEquals("bp", blueprints.idOf(result.stack()).orElseThrow());
    }

    @Test
    void configReadsEntriesAndSkipsBadOnes() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("""
                structure-loot:
                  - {table: "ancient_city", item: Relic, chance: 0.5, job: mining, min-level: 25}
                  - {table: "a:b", item: relic, blueprint: bp, chance: 0.5, job: mining, min-level: 25}
                  - {table: "a:b", item: relic, chance: 2, job: mining, min-level: 25}
                  - {table: "a:b", blueprint: bp, chance: 0.1, job: mining, min-level: 20}
                """);
        StructureLootConfig config = StructureLootConfig.load(yaml, Logger.getAnonymousLogger());
        assertEquals(
                List.of(
                        new Entry("minecraft:ancient_city", false, "relic", 0.5, "mining", 25),
                        new Entry("a:b", true, "bp", 0.1, "mining", 20)),
                config.entries());
        assertEquals(
                StructureLootConfig.defaults(),
                StructureLootConfig.load(new YamlConfiguration(), Logger.getAnonymousLogger()));
    }

    @Test
    void shippedConfigMatchesTheDefaults() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        try (var reader = new java.io.InputStreamReader(
                getClass().getResourceAsStream("/config.yml"), java.nio.charset.StandardCharsets.UTF_8)) {
            yaml.load(reader);
        }
        assertTrue(yaml.contains("structure-loot"));
        assertEquals(
                StructureLootConfig.defaults(), StructureLootConfig.load(yaml, Logger.getAnonymousLogger()));
    }
}
