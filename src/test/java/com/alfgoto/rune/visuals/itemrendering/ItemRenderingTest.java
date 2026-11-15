package com.alfgoto.rune.visuals.itemrendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class ItemRenderingTest {

    private ItemRegistry registry;
    private RuneRegistry runes;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        registry = new ItemRegistry(plugin);
        registry.register(new RuneItem("rare_thing", Rarity.RARE, Material.PAPER, "test_rune"));
        runes = new RuneRegistry(plugin, registry, new CapacityCost(RuneConfig.defaults()));
        runes.register(new Rune(
                new RuneItem("test_rune", Rarity.RARE, Material.PAPER, "test_rune"),
                Effect.builder("test_rune", EffectTriggers.BLOCK_BREAK)
                        .does(context -> {})
                        .with(5, 5, 4),
                Set.of(Fit.PLAYER, Fit.PICKAXE)));
        registry.addRedrawer(new ItemRenderer(runes, registry::toolStats));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void nameHasRarityColourAndTranslationKey() {
        ItemStack stack = registry.create(registry.get("rare_thing"), 1);
        TranslatableComponent name = (TranslatableComponent) stack.getItemMeta().itemName();
        assertEquals("rune.item.rare_thing", name.key());
        assertEquals("Rare Thing", name.fallback());
        assertEquals(Rarity.RARE.color(), name.color());
    }

    @Test
    void loreShowsRarityAndStatsPlaceholder() {
        ItemStack stack = registry.create(registry.get("rare_thing"), 1);
        List<Component> lore = stack.getItemMeta().lore();
        assertEquals(2, lore.size());
        assertEquals("rune.rarity.rare", ((TranslatableComponent) lore.get(0)).key());
        assertEquals("rune.item.lore.stats_placeholder", ((TranslatableComponent) lore.get(1)).key());
    }

    @Test
    void runeLoreShowsRarityRankCostAndFits() {
        ItemStack stack = runes.create(runes.get("test_rune"), 2);
        List<Component> lore = stack.getItemMeta().lore();
        assertEquals(4, lore.size());
        assertEquals("rune.rarity.rare", ((TranslatableComponent) lore.get(0)).key());
        TranslatableComponent rank = (TranslatableComponent) lore.get(1);
        assertEquals("rune.item.lore.rank_max", rank.key());
        assertEquals(List.of(Component.text(2), Component.text(4)), rank.arguments().stream()
                .map(a -> a.asComponent())
                .toList());
        TranslatableComponent cost = (TranslatableComponent) lore.get(2);
        assertEquals("rune.item.lore.capacity_cost", cost.key());
        assertEquals(Component.text(4), cost.arguments().get(0).asComponent()); // Rare 3 + 1 per Rank above 1
        TranslatableComponent fits = (TranslatableComponent) lore.get(3);
        assertEquals("rune.item.lore.fits", fits.key());
    }

    @Test
    void toolLoreShowsHarvestTierSlotsAndCapacity() {
        registry.register(RuneItem.tool("lore_pick", Rarity.RARE, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        ItemStack stack = registry.create(registry.get("lore_pick"), 1);
        List<Component> lore = stack.getItemMeta().lore();
        assertEquals("rune.item.lore.harvest_tier", ((TranslatableComponent) lore.get(1)).key());
        TranslatableComponent slots = (TranslatableComponent) lore.get(2);
        assertEquals("rune.item.lore.slots", slots.key());
        assertEquals(Component.text(3).color(NamedTextColor.WHITE), slots.arguments().get(0).asComponent());
        TranslatableComponent capacity = (TranslatableComponent) lore.get(3);
        assertEquals("rune.item.lore.capacity", capacity.key());
        assertEquals(Component.text(10).color(NamedTextColor.WHITE), capacity.arguments().get(0).asComponent());
    }

    @Test
    void changingDataRedrawsTheLore() {
        ItemStack stack = registry.create(registry.get("rare_thing"), 1);
        stack.editMeta(m -> m.lore(List.of(Component.text("old"))));
        registry.update(stack, m -> {});
        assertEquals(2, stack.getItemMeta().lore().size());
    }

    @Test
    void vanillaStackIsLeftAlone() {
        ItemStack stack = new ItemStack(Material.PAPER);
        registry.update(stack, m -> {});
        registry.redraw(stack);
        assertNull(stack.getItemMeta().lore());
    }
}
