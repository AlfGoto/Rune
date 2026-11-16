package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.Scale;
import com.alfgoto.rune.gameplay.effects.ScaleRegistry;
import com.alfgoto.rune.gameplay.items.ArmorKind;
import com.alfgoto.rune.gameplay.items.ArmorSpec;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.items.TraitBinding;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** Traits (#178): Trait bindings stack with Runes, can be negative, change Capacity and count when worn. */
class TraitTest {

    private ServerMock server;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private Rune rune(String id, double perRank, Fit... fits) {
        Effect<?> effect = Effect.builder(id, EffectTriggers.BLOCK_BREAK).does(context -> {});
        return runes.register(new Rune(
                new RuneItem(id, Rarity.COMMON, Material.PAPER, "test_rune"), effect.with(perRank, perRank, 5), Set.of(fits)));
    }

    private ItemStack pick(String id, ToolSpec spec) {
        return items.create(items.register(RuneItem.tool(id, Rarity.RARE, ToolKind.PICKAXE, "test_rune", spec)), 1);
    }

    @Test
    void scaleRegistryHasAlways() {
        ScaleRegistry scales = new ScaleRegistry();
        assertEquals(1, scales.get("always").at(null, null));
        assertThrows(IllegalArgumentException.class, () -> scales.get("depth"));
    }

    @Test
    void traitStacksWithInstalledRunesAndComesFirst() {
        Rune smelt = rune("smelt", 50, Fit.PICKAXE);
        ItemStack pick = pick("smelt_pick", ToolSpec.DEFAULT.withTrait("fiery", 0, TraitBinding.always("smelt", 100)));
        loadout.install(pick, smelt, 1); // +50

        var sources = loadout.sources(pick);
        assertEquals(2, sources.size());
        assertEquals(100, sources.get(0).value()); // the Trait is first
        assertEquals(150, loadout.total(pick, "smelt"));
    }

    @Test
    void negativeValueLowersTheTotal() {
        Rune haste = rune("haste", 8, Fit.PICKAXE);
        ItemStack pick = pick("slow_pick", ToolSpec.DEFAULT.withTrait("slow", 0, TraitBinding.always("haste", -20)));
        assertEquals(-20, loadout.total(pick, "haste"));
        loadout.install(pick, haste, 1); // +8
        assertEquals(-12, loadout.total(pick, "haste"));
    }

    @Test
    void valueIsMultipliedByScale() {
        Scale half = new Scale() {
            @Override
            public String id() {
                return "half";
            }

            @Override
            public double at(org.bukkit.entity.Player player, ItemStack stack) {
                return 0.5;
            }
        };
        ItemStack pick = pick("half_pick", ToolSpec.DEFAULT.withTrait("half", 0, new TraitBinding("haste", 40, half)));
        assertEquals(20, loadout.total(pick, "haste"));
    }

    @Test
    void capacityModifierChangesCapacityAndTheCheck() {
        Rune cheap = rune("cheap", 1, Fit.PICKAXE);
        ItemStack plain = pick("plain_pick", ToolSpec.DEFAULT); // Rare: Capacity 10
        ItemStack less = pick("less_pick", ToolSpec.DEFAULT.withTrait("heavy", -8)); // 2
        ItemStack more = pick("more_pick", ToolSpec.DEFAULT.withTrait("light", 1)); // 11

        assertEquals(10, loadout.capacity(plain));
        assertEquals(2, loadout.capacity(less));
        assertEquals(11, loadout.capacity(more));

        assertEquals(ToolLoadout.Result.INSTALLED, loadout.install(less, cheap, 2)); // cost 2
        assertEquals(ToolLoadout.Result.OVER_CAPACITY, loadout.install(less, cheap, 1));
    }

    @Test
    void capacityNeverGoesBelowZero() {
        ItemStack pick = pick("empty_pick", ToolSpec.DEFAULT.withTrait("awful", -50));
        assertEquals(0, loadout.capacity(pick));
    }

    @Test
    void wornArmorTraitsAreCounted() {
        items.register(RuneItem.armor(
                "trait_helmet",
                Rarity.COMMON,
                "test_rune",
                ArmorSpec.of(ArmorKind.HELMET).withTrait("keen", 1, TraitBinding.always("haste", 10))));
        items.register(RuneItem.armor(
                "trait_boots",
                Rarity.COMMON,
                "test_rune",
                ArmorSpec.of(ArmorKind.BOOTS).withTrait("heavy", 0, TraitBinding.always("haste", -4))));
        ItemStack helmet = items.create(items.get("trait_helmet"), 1);
        ItemStack boots = items.create(items.get("trait_boots"), 1);
        assertEquals(4, loadout.capacity(helmet)); // Common 3 + 1

        PlayerMock player = server.addPlayer();
        assertEquals(0, loadout.wornTotal(player, "haste"));
        player.getInventory().setHelmet(helmet);
        player.getInventory().setBoots(boots);
        assertEquals(6, loadout.wornTotal(player, "haste"));
        assertEquals(List.of(10.0, -4.0), loadout.wornSources(player).stream().map(s -> s.value()).toList());
    }
}
