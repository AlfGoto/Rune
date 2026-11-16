package com.alfgoto.rune.gameplay.caches;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.rewards.LootRoller;
import com.alfgoto.rune.gameplay.rewards.LootTable;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RuneReward;
import com.alfgoto.rune.text.Rarity;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class CachesTest {

    private static final Job MINING = Job.builder("mining").build();

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private ItemRegistry items;
    private RuneRegistry runes;
    private Caches caches;
    private final RuneConfig config = RuneConfig.defaults();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        items = new ItemRegistry(plugin);
        Caches.itemsToRegister().forEach(items::register);
        runes = new RuneRegistry(plugin, items, new CapacityCost(config));
        rune("general", null);
        rune("mining_rune", MINING);
        LootTableRegistry tables = new LootTableRegistry();
        tables.register(LootTable.builder(Caches.TABLE)
                .add(new RuneReward(runes, config, Rarity.COMMON), 1, Rarity.COMMON)
                .build());
        caches = new Caches(plugin, items, tables, new LootRoller(config), new Random(7));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void rune(String id, Job job) {
        Effect<?> effect = Effect.builder("fx_" + id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        runes.register(new Rune(
                new RuneItem(id, Rarity.COMMON, Material.PAPER, "test_rune"), effect.with(5, 5), Set.of(Fit.PLAYER), job));
    }

    private int runeCount() {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && runes.of(stack).isPresent()) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    @Test
    void aCacheKeepsItsRarityAndJob() {
        ItemStack stack = caches.create(Rarity.RARE, "mining");
        assertEquals(Rarity.RARE, caches.rarityOf(stack).orElseThrow());
        assertEquals("mining", caches.jobOf(stack).orElseThrow());
        assertTrue(caches.jobOf(caches.create(Rarity.RARE, null)).isEmpty());
        assertTrue(caches.rarityOf(new ItemStack(Material.PAPER)).isEmpty());
    }

    @Test
    void openingGivesLootAndTakesOneCache() {
        ItemStack stack = caches.create(Rarity.COMMON, null);
        stack.setAmount(2);
        assertTrue(caches.open(player, stack));
        assertEquals(1, stack.getAmount());
        assertEquals(1, runeCount());
    }

    @Test
    void aLegendaryCacheRollsSeveralTimes() {
        assertTrue(caches.open(player, caches.create(Rarity.LEGENDARY, null)));
        assertEquals(config.rewardsRollsByRarity().get(Rarity.LEGENDARY.ordinal()), runeCount());
    }

    @Test
    void aVanillaStackIsNotOpened() {
        assertFalse(caches.open(player, new ItemStack(Material.PAPER)));
        assertEquals(0, runeCount());
    }

    @Test
    void aJobCacheLeansTowardsItsJobSixtyPercentOfTheTime() {
        int job = 0;
        int total = 4000;
        for (int i = 0; i < total; i++) {
            PlayerMock fresh = server.addPlayer();
            caches.open(fresh, caches.create(Rarity.COMMON, "mining"));
            ItemStack got = fresh.getInventory().getItem(0);
            if (runes.of(got).orElseThrow().id().equals("mining_rune")) {
                job++;
            }
        }
        assertEquals(0.6, job / (double) total, 0.03);
    }

    @Test
    void aCacheWithNoJobGivesOnlyGeneralRunes() {
        for (int i = 0; i < 200; i++) {
            PlayerMock fresh = server.addPlayer();
            caches.open(fresh, caches.create(Rarity.COMMON, null));
            assertEquals("general", runes.of(fresh.getInventory().getItem(0)).orElseThrow().id());
        }
    }

    /** The plugin's own listener (real services, real {@code cache} table) opens the Cache and takes it. */
    @Test
    void rightClickOpensTheCacheThroughTheListener() {
        for (Action action : new Action[] {Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK}) {
            ItemStack cache = caches.create(Rarity.COMMON, null); // same item id as the plugin's own Cache
            player.getInventory().setItemInMainHand(cache);
            Block block = player.getWorld().getBlockAt(0, 64, 0);
            server.getPluginManager()
                    .callEvent(new PlayerInteractEvent(player, action, cache, block, BlockFace.UP, EquipmentSlot.HAND));
            assertEquals(Material.AIR, player.getInventory().getItemInMainHand().getType(), action.name());
        }
    }
}
