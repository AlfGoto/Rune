package com.alfgoto.rune.gameplay.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.ArmorConfig;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.runes.ToolLoadout.Result;
import com.alfgoto.rune.overrides.LockedGear;
import com.alfgoto.rune.text.Rarity;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class RuneArmorTest {

    private RunePlugin plugin;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        items = new ItemRegistry(plugin, ToolConfig.defaults(), ArmorConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private RuneItem piece(String id, Rarity rarity, ArmorKind kind) {
        return items.register(RuneItem.armor(id, rarity, "test_rune", ArmorSpec.of(kind)));
    }

    private Rune rune(String id, Rarity rarity, Fit... fits) {
        Effect<?> effect = Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return runes.register(new Rune(
                new RuneItem(id, rarity, Material.PAPER, "test_rune"), effect.with(10, 10), Set.of(fits)));
    }

    @Test
    void protectionFollowsRarityAndPiece() {
        ArmorStats leatherHelmet = items.armorStats(piece("c_helmet", Rarity.COMMON, ArmorKind.HELMET)).orElseThrow();
        assertEquals(1, leatherHelmet.defense());
        assertEquals(55, leatherHelmet.durability());
        assertEquals(1, leatherHelmet.slots());
        assertEquals(3, leatherHelmet.capacity());

        ArmorStats netheriteChest =
                items.armorStats(piece("l_chest", Rarity.LEGENDARY, ArmorKind.CHESTPLATE)).orElseThrow();
        assertEquals(8, netheriteChest.defense());
        assertEquals(3.0, netheriteChest.toughness());
        assertEquals(592, netheriteChest.durability());
        assertEquals(5, netheriteChest.slots());
        assertEquals(18, netheriteChest.capacity());
    }

    @Test
    void aPieceCanOverrideItsStats() {
        RuneItem item = items.register(RuneItem.armor(
                "odd_boots", Rarity.COMMON, "test_rune", new ArmorSpec(ArmorKind.BOOTS, 4, 1.0, 100, 2, 9, "odd")));
        ArmorStats stats = items.armorStats(item).orElseThrow();
        assertEquals(4, stats.defense());
        assertEquals(100, stats.durability());
        assertEquals(2, stats.slots());
        assertEquals(9, stats.capacity());
        assertEquals("odd", stats.trait().orElseThrow());
    }

    @Test
    void theStackGivesTheProtectionAndDurabilityThroughComponents() {
        ItemStack boots = items.create(piece("e_boots", Rarity.EPIC, ArmorKind.BOOTS), 1);
        assertEquals(429, boots.getData(DataComponentTypes.MAX_DAMAGE));
        var modifiers = boots.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers();
        assertEquals(2, modifiers.size()); // armor + toughness
        assertTrue(modifiers.stream().anyMatch(e -> e.modifier().getAmount() == 3.0));
    }

    @Test
    void runeArmorIsNeverLockedGear() {
        ItemStack helmet = items.create(piece("c_helmet", Rarity.COMMON, ArmorKind.HELMET), 1);
        assertEquals(Material.LEATHER_HELMET, helmet.getType());
        assertFalse(LockedGear.isLocked(helmet));
        assertTrue(LockedGear.isLocked(new ItemStack(Material.LEATHER_HELMET)));
    }

    @Test
    void passiveRunesInstallOnArmorAndTravelWithIt() {
        ItemStack helmet = items.create(piece("r_helmet", Rarity.RARE, ArmorKind.HELMET), 1); // 3 slots, Capacity 10
        Rune player = rune("speed", Rarity.COMMON, Fit.PLAYER);
        Rune helmetOnly = rune("night_sight", Rarity.COMMON, Fit.HELMET);
        Rune boots = rune("light_step", Rarity.COMMON, Fit.BOOTS);
        Rune pickaxe = rune("ore_sense", Rarity.COMMON, Fit.PICKAXE);

        assertEquals(Result.INSTALLED, loadout.install(helmet, player, 1));
        assertEquals(Result.INSTALLED, loadout.install(helmet, helmetOnly, 2));
        assertEquals(Result.DOES_NOT_FIT, loadout.install(helmet, boots, 1));
        assertEquals(Result.DOES_NOT_FIT, loadout.install(helmet, pickaxe, 1));
        assertEquals(1 + 2, loadout.used(helmet));
        assertEquals(10, loadout.capacity(helmet));
        assertEquals(2, loadout.installed(helmet.clone()).size());
        assertEquals(10.0, loadout.total(helmet, "speed"));
        assertEquals(20.0, loadout.total(helmet, "night_sight"));

        assertEquals(1, loadout.uninstall(helmet, 1).orElseThrow().rank());
        assertEquals(1, loadout.installed(helmet).size());
    }

    @Test
    void armorLoadoutHonoursSlotsAndCapacity() {
        ItemStack helmet = items.create(piece("c_helmet", Rarity.COMMON, ArmorKind.HELMET), 1); // 1 slot, Capacity 3
        Rune speed = rune("speed", Rarity.COMMON, Fit.PLAYER);
        assertEquals(Result.OVER_CAPACITY, loadout.install(helmet, speed, 5)); // cost 1 + 4 = 5 > 3
        assertEquals(Result.INSTALLED, loadout.install(helmet, speed, 1));
        assertEquals(Result.NO_FREE_SLOT, loadout.install(helmet, speed, 1));
    }

    @Test
    void anItemCannotBeToolAndArmor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RuneItem(
                        "x",
                        Rarity.COMMON,
                        Material.LEATHER_HELMET,
                        "test_rune",
                        ToolSpec.DEFAULT,
                        ArmorSpec.of(ArmorKind.HELMET)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new RuneItem(
                        "y", Rarity.COMMON, Material.PAPER, "test_rune", null, ArmorSpec.of(ArmorKind.HELMET)));
    }

    @Test
    void brokenArmorStaysWithItsRunesAndGivesNoProtection() {
        RuneItem helmet = piece("broken_helmet", Rarity.COMMON, ArmorKind.HELMET);
        ItemStack stack = items.create(helmet, 1);
        assertTrue(stack.hasData(DataComponentTypes.ATTRIBUTE_MODIFIERS));
        BrokenToolListener listener = new BrokenToolListener(items);
        org.bukkit.entity.Player player = MockBukkit.getMock().addPlayer();
        org.bukkit.inventory.meta.Damageable meta = (org.bukkit.inventory.meta.Damageable) stack.getItemMeta();
        meta.setDamage(meta.getMaxDamage() - 1);
        stack.setItemMeta(meta);
        org.bukkit.event.player.PlayerItemDamageEvent event =
                new org.bukkit.event.player.PlayerItemDamageEvent(player, stack, 1);
        listener.onItemDamage(event);
        assertTrue(event.isCancelled());
        assertTrue(listener.isBroken(stack));
        assertTrue(items.of(stack).isPresent());
        var modifiers = stack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        assertTrue(modifiers == null || modifiers.modifiers().isEmpty());
    }

    @Test
    void aBrokenHelmetInHandDoesNotBlockMining() {
        RuneItem helmet = piece("held_helmet", Rarity.COMMON, ArmorKind.HELMET);
        ItemStack stack = items.create(helmet, 1);
        stack.setData(DataComponentTypes.DAMAGE, stack.getData(DataComponentTypes.MAX_DAMAGE));
        BrokenToolListener listener = new BrokenToolListener(items);
        org.bukkit.entity.Player player = MockBukkit.getMock().addPlayer();
        player.getInventory().setItemInMainHand(stack);
        org.bukkit.block.Block block = player.getWorld().getBlockAt(0, 64, 0);
        org.bukkit.event.block.BlockBreakEvent event = new org.bukkit.event.block.BlockBreakEvent(block, player);
        listener.onBlockBreak(event);
        assertFalse(event.isCancelled());
    }

    @Test
    void theTestHelmetIsRegistered() {
        assertTrue(plugin.getServer().getPluginManager().isPluginEnabled(plugin));
        ItemRegistry registry = plugin.context().get(ItemRegistry.class);
        assertTrue(registry.find("test_helmet").flatMap(i -> i.armorSpec()).isPresent());
    }
}
