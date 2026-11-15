package com.alfgoto.rune.gameplay.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.text.Rarity;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import net.kyori.adventure.util.TriState;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class RuneToolTest {

    private final ToolConfig config = ToolConfig.defaults();
    private ItemRegistry registry;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        registry = new ItemRegistry(plugin, config);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static RuneItem pickaxe(Rarity rarity, ToolSpec spec) {
        return RuneItem.tool("pick_" + rarity.id(), rarity, ToolKind.PICKAXE, "test_rune", spec);
    }

    private ToolStats stats(RuneItem item) {
        return registry.toolStats(item).orElseThrow();
    }

    /** What the tool component says about {@code block}: its speed, or -1 when it gives no drops. */
    private static float speedOn(ItemStack stack, Material block) {
        Tool tool = stack.getData(DataComponentTypes.TOOL);
        for (Tool.Rule rule : tool.rules()) {
            if (rule.blocks().contains(io.papermc.paper.registry.TypedKey.create(
                    io.papermc.paper.registry.RegistryKey.BLOCK, block.getKey()))) {
                if (rule.correctForDrops() == TriState.FALSE) {
                    return -1;
                }
                return rule.speed();
            }
        }
        return tool.defaultMiningSpeed();
    }

    @Test
    void harvestTierDefaultsFromRarity() {
        assertEquals(HarvestTier.STONE, stats(pickaxe(Rarity.COMMON, ToolSpec.DEFAULT)).tier());
        assertEquals(HarvestTier.IRON, stats(pickaxe(Rarity.UNCOMMON, ToolSpec.DEFAULT)).tier());
        assertEquals(HarvestTier.DIAMOND, stats(pickaxe(Rarity.RARE, ToolSpec.DEFAULT)).tier());
        assertEquals(HarvestTier.DIAMOND, stats(pickaxe(Rarity.LEGENDARY, ToolSpec.DEFAULT)).tier());
    }

    @Test
    void harvestTierCanBeOneAboveOrBelow() {
        assertEquals(HarvestTier.DIAMOND, stats(pickaxe(Rarity.UNCOMMON, ToolSpec.DEFAULT.withTierOffset(1))).tier());
        assertEquals(HarvestTier.STONE, stats(pickaxe(Rarity.UNCOMMON, ToolSpec.DEFAULT.withTierOffset(-1))).tier());
        assertThrows(
                IllegalArgumentException.class,
                () -> stats(pickaxe(Rarity.RARE, ToolSpec.DEFAULT.withTierOffset(1))));
        assertThrows(IllegalArgumentException.class, () -> ToolSpec.DEFAULT.withTierOffset(2));
    }

    @Test
    void speedAndDurabilityFollowTierAndEpicGetsGoldAndNetherite() {
        ToolStats iron = stats(pickaxe(Rarity.UNCOMMON, ToolSpec.DEFAULT));
        assertEquals(6f, iron.speed());
        assertEquals(250, iron.durability());
        ToolStats rare = stats(pickaxe(Rarity.RARE, ToolSpec.DEFAULT));
        assertEquals(8f, rare.speed());
        assertEquals(1561, rare.durability());
        ToolStats epic = stats(pickaxe(Rarity.EPIC, ToolSpec.DEFAULT));
        assertEquals(12f, epic.speed());
        assertEquals(2031, epic.durability());
    }

    @Test
    void slotsAndCapacityFollowRarityAndCanBeOverridden() {
        ToolStats common = stats(pickaxe(Rarity.COMMON, ToolSpec.DEFAULT));
        assertEquals(1, common.slots());
        assertEquals(3, common.capacity());
        ToolStats legendary = stats(pickaxe(Rarity.LEGENDARY, ToolSpec.DEFAULT));
        assertEquals(5, legendary.slots());
        assertEquals(18, legendary.capacity());
        ToolStats custom = stats(pickaxe(
                Rarity.COMMON, new ToolSpec(0, 5f, 99, 2, 7, "fiery")));
        assertEquals(5f, custom.speed());
        assertEquals(99, custom.durability());
        assertEquals(2, custom.slots());
        assertEquals(7, custom.capacity());
        assertEquals("fiery", custom.trait().orElseThrow());
    }

    @Test
    void aToolNeedsAToolMaterial() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RuneItem("x", Rarity.COMMON, Material.PAPER, "test_rune", ToolSpec.DEFAULT));
        assertFalse(new RuneItem("x", Rarity.COMMON, Material.PAPER, "test_rune").toolSpec().isPresent());
    }

    @Test
    void stoneTierPickaxeMinesIronButNotDiamond() {
        RuneItem item = pickaxe(Rarity.COMMON, ToolSpec.DEFAULT);
        registry.register(item);
        ItemStack stack = registry.create(item, 1);
        assertEquals(4f, speedOn(stack, Material.IRON_ORE));
        assertEquals(-1f, speedOn(stack, Material.DIAMOND_ORE));
        assertEquals(4f, speedOn(stack, Material.STONE));
        assertEquals(1f, speedOn(stack, Material.DIRT));
        assertEquals(131, stack.getData(DataComponentTypes.MAX_DAMAGE));
    }

    @Test
    void ironTierPickaxeMinesDiamondButNotObsidian() {
        RuneItem item = pickaxe(Rarity.UNCOMMON, ToolSpec.DEFAULT);
        registry.register(item);
        ItemStack stack = registry.create(item, 1);
        assertEquals(6f, speedOn(stack, Material.DIAMOND_ORE));
        assertEquals(-1f, speedOn(stack, Material.OBSIDIAN));
        assertEquals(250, stack.getData(DataComponentTypes.MAX_DAMAGE));
    }

    @Test
    void diamondTierPickaxeMinesDiamond() {
        RuneItem item = pickaxe(Rarity.RARE, ToolSpec.DEFAULT);
        registry.register(item);
        ItemStack stack = registry.create(item, 1);
        assertEquals(8f, speedOn(stack, Material.OBSIDIAN));
        assertEquals(8f, speedOn(stack, Material.IRON_ORE));
        assertTrue(registry.of(stack).isPresent());
    }
}
