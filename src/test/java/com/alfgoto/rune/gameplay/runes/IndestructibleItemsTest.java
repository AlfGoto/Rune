package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class IndestructibleItemsTest {

    private static final List<DamageCause> SOURCES = List.of(
            DamageCause.FIRE,
            DamageCause.FIRE_TICK,
            DamageCause.LAVA,
            DamageCause.CONTACT,
            DamageCause.BLOCK_EXPLOSION,
            DamageCause.ENTITY_EXPLOSION,
            DamageCause.LIGHTNING);

    private ServerMock server;
    private World world;
    private ItemStack rune;
    private ItemStack tool;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        ItemRegistry items = new ItemRegistry(plugin);
        RuneRegistry runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        Effect<?> effect = Effect.builder("test_effect", EffectTriggers.BLOCK_BREAK).does(context -> {});
        Rune testRune = runes.register(new Rune(
                new RuneItem("indestructible_rune", Rarity.COMMON, Material.PAPER, "test_rune"),
                effect.with(5, 5),
                Set.of(Fit.PLAYER)));
        rune = runes.create(testRune, 1);
        RuneItem pick = items.register(
                RuneItem.tool("indestructible_pick", Rarity.COMMON, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        tool = items.create(pick, 1);
        server.getPluginManager().registerEvents(new IndestructibleItems(items, runes), plugin);
        world = server.addSimpleWorld("indestructible");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Item drop(ItemStack stack) {
        return world.dropItem(new Location(world, 0, 64, 0), stack);
    }

    private EntityDamageEvent damage(Item item, DamageCause cause) {
        EntityDamageEvent event = new EntityDamageEvent(item, cause, 4.0);
        server.getPluginManager().callEvent(event);
        return event;
    }

    @Test
    void everyDamageSourceIsCancelledForRunesAndGear() {
        for (ItemStack stack : List.of(rune, tool)) {
            Item item = drop(stack);
            for (DamageCause cause : SOURCES) {
                assertTrue(damage(item, cause).isCancelled(), cause + " must be cancelled");
            }
            assertFalse(item.isDead());
        }
    }

    @Test
    void vanillaItemsStillTakeDamage() {
        assertFalse(damage(drop(new ItemStack(Material.PAPER)), DamageCause.FIRE).isCancelled());
    }

    @Test
    void runeItemsNeverDespawn() {
        for (ItemStack stack : List.of(rune, tool)) {
            ItemDespawnEvent event = new ItemDespawnEvent(drop(stack), new Location(world, 0, 64, 0));
            server.getPluginManager().callEvent(event);
            assertTrue(event.isCancelled());
        }
        ItemDespawnEvent vanilla =
                new ItemDespawnEvent(drop(new ItemStack(Material.PAPER)), new Location(world, 0, 64, 0));
        server.getPluginManager().callEvent(vanilla);
        assertFalse(vanilla.isCancelled());
    }

    @Test
    void droppedRuneItemsHaveUnlimitedLifetime() {
        assertTrue(drop(rune).isUnlimitedLifetime());
        assertFalse(drop(new ItemStack(Material.PAPER)).isUnlimitedLifetime());
    }

    @Test
    void voidSendsTheItemToTheWorldSpawn() {
        Item item = world.dropItem(new Location(world, 500, -80, 500), rune);
        assertTrue(damage(item, DamageCause.VOID).isCancelled());
        Location spawn = world.getSpawnLocation();
        assertEquals(spawn.getBlockX(), item.getLocation().getBlockX());
        assertEquals(spawn.getBlockZ(), item.getLocation().getBlockZ());
        assertFalse(item.isDead());
    }
}
