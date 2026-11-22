package com.alfgoto.rune.visuals.itemrendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Rarity;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class RuneModelRendererTest {

    private ItemRegistry registry;
    private RuneRegistry runes;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        registry = new ItemRegistry(plugin);
        registry.register(new RuneItem("rune_cache_rare", Rarity.RARE, Material.PAPER, "rune_cache_rare"));
        runes = new RuneRegistry(plugin, registry, new CapacityCost(RuneConfig.defaults()));
        runes.register(new Rune(
                new RuneItem("haste", Rarity.COMMON, Material.PAPER, "haste"),
                Effect.builder("haste", EffectTriggers.BLOCK_BREAK).does(context -> {}).with(8, 8, 5),
                Set.of(Fit.PICKAXE)));
        registry.addRedrawer(new RuneModelRenderer(runes));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void runeUsesItsOwnModel() {
        ItemStack stack = runes.create(runes.get("haste"), 1);
        assertEquals(new NamespacedKey("rune", "haste"), stack.getItemMeta().getItemModel());
    }

    @Test
    void rankGoesIntoCustomModelData() {
        ItemStack stack = runes.create(runes.get("haste"), 3);
        assertEquals(List.of(3f), stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA).floats());
    }

    @Test
    void otherItemsGetNoCustomModelData() {
        ItemStack stack = registry.create(registry.get("rune_cache_rare"), 1);
        assertFalse(stack.hasData(DataComponentTypes.CUSTOM_MODEL_DATA));
        assertEquals(new NamespacedKey("rune", "rune_cache_rare"), stack.getItemMeta().getItemModel());
    }
}
