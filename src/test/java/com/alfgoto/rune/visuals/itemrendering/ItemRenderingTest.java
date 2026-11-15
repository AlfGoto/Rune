package com.alfgoto.rune.visuals.itemrendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class ItemRenderingTest {

    private ItemRegistry registry;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        registry = new ItemRegistry(plugin);
        registry.register(new RuneItem("rare_thing", Rarity.RARE, Material.PAPER, "test_rune"));
        registry.addRedrawer(new ItemRenderer());
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
