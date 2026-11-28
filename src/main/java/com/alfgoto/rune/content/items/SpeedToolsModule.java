package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.config.TraitConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.crafting.CraftingModule;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.durability.Sturdy;
import com.alfgoto.rune.gameplay.effects.Scale;
import com.alfgoto.rune.gameplay.effects.ScaleRegistry;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.items.TraitBinding;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.traits.CopperOxidation;
import com.alfgoto.rune.gameplay.traits.Momentum;
import com.alfgoto.rune.gameplay.traits.TraitsModule;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;

/**
 * The tools of the Copper and Momentum Traits (docs/jobs/mining.md, woodcutting.md): Copper Pickaxe, Copper Axe,
 * Redstone Pickaxe and Breeze Axe. Copper is faster as it oxidizes (0, +10, +20, +30%); Momentum is faster per
 * block in a row (+5% per block, up to +50%). Both are a {@code haste} Trait binding on a Scale. Numbers: config
 * {@code tools.copper} and {@code tools.momentum}.
 *
 * <p>Recipes are the documented ones. The Copper Axe upgrades the Stone Hatchet (Woodcutting 4).
 */
@AutoService(RuneModule.class)
public final class SpeedToolsModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                ItemsModule.class,
                TraitsModule.class,
                CraftingModule.class,
                CoreItemsModule.class,
                CoreJobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        ScaleRegistry scales = ctx.get(ScaleRegistry.class);
        TraitConfig config = ctx.config().traits();
        Scale copperStage = scales.get(CopperOxidation.SCALE_ID);
        Scale momentum = scales.get(Momentum.SCALE_ID);

        // Uncommon, Harvest Tier stone (one below the Rarity default), Capacity 6.
        // The model is the first stage; the stage and the wax change it (CopperOxidation).
        ToolSpec copper = ToolSpec.DEFAULT.withTierOffset(-1);
        items.register(RuneItem.tool("copper_pickaxe", Rarity.UNCOMMON, ToolKind.PICKAXE, "copper_pickaxe",
                copper.withTrait("copper_pickaxe", 0, new TraitBinding("haste", config.copperSpeed(), copperStage))));
        items.register(RuneItem.tool("copper_axe", Rarity.UNCOMMON, ToolKind.AXE, "copper_axe",
                copper.withTrait("copper_axe", 0, new TraitBinding("haste", config.copperSpeed(), copperStage))));

        // Rare, diamond tier, Capacity 10 plus the modifier (tools.momentum.capacity-modifier, -2 by default).
        int capacityModifier = config.momentumCapacityModifier();
        items.register(RuneItem.tool("redstone_pickaxe", Rarity.RARE, ToolKind.PICKAXE, "redstone_pickaxe",
                ToolSpec.DEFAULT.withTrait("redstone_pickaxe", capacityModifier,
                        new TraitBinding("haste", config.momentumSpeed(), momentum))));
        items.register(RuneItem.tool("breeze_axe", Rarity.RARE, ToolKind.AXE, "breeze_axe",
                ToolSpec.DEFAULT.withTrait("breeze_axe", capacityModifier,
                        new TraitBinding("haste", config.momentumSpeed(), momentum))));

        // Epic, diamond tier, Capacity 14 -3 (docs/jobs/woodcutting.md): a built-in Treefeller and more durability
        // use (a negative Sturdy). Numbers: config traits.tempest_axe.
        var cfg = ctx.config();
        double blocks = cfg.traitNumber("tempest_axe", "blocks", 16);
        double extraUse = cfg.traitNumber("tempest_axe", "extra_durability_percent", 50);
        int tempestCapacity = (int) -cfg.traitNumber("tempest_axe", "capacity_penalty", 3);
        items.register(RuneItem.tool("tempest_axe", Rarity.EPIC, ToolKind.AXE, "tempest_axe",
                ToolSpec.DEFAULT.withTrait("tempest_axe", tempestCapacity,
                        TraitBinding.always("treefeller", blocks),
                        TraitBinding.always(Sturdy.ID, -extraUse))));

        recipes(ctx);
    }

    /**
     * Copper Pickaxe (Mining 4), Redstone Pickaxe (Mining 11) and Breeze Axe (Woodcutting 11). The ingredients
     * are the documented ones; the grid layouts are proposed (not in docs). Each recipe consumes the previous tool
     * (an Upgrade), so its Runes carry over.
     */
    private static void recipes(RuneContext ctx) {
        RecipeBook book = ctx.get(RecipeBook.class);
        JobRegistry jobs = ctx.get(JobRegistry.class);
        Job mining = jobs.registry().get("mining");
        Job woodcutting = jobs.registry().get("woodcutting");

        book.register(new CraftingRecipe("copper_pickaxe", mining, 4, "copper_pickaxe", 1,
                List.of("CCC", "HP "), Map.of(
                        'C', Ingredient.vanilla(Material.COPPER_INGOT),
                        'H', Ingredient.rune("reinforced_handle"),
                        'P', Ingredient.rune("coal_pickaxe"))));
        book.register(new CraftingRecipe("copper_axe", woodcutting, 4, "copper_axe", 1,
                List.of("CCC", "HA "), Map.of(
                        'C', Ingredient.vanilla(Material.COPPER_INGOT),
                        'H', Ingredient.rune("reinforced_handle"),
                        'A', Ingredient.rune("stone_hatchet"))));
        book.register(new CraftingRecipe("redstone_pickaxe", mining, 11, "redstone_pickaxe", 1,
                List.of("RDR", "GP "), Map.of(
                        'R', Ingredient.vanilla(Material.REDSTONE_BLOCK),
                        'D', Ingredient.vanilla(Material.DIAMOND),
                        'G', Ingredient.rune("gem_setting"),
                        'P', Ingredient.rune("copper_pickaxe"))));
        book.register(new CraftingRecipe("breeze_axe", woodcutting, 11, "breeze_axe", 1,
                List.of("BBB", "DAG"), Map.of(
                        'B', Ingredient.vanilla(Material.BREEZE_ROD),
                        'D', Ingredient.vanilla(Material.DIAMOND),
                        'G', Ingredient.rune("gem_setting"),
                        'A', Ingredient.rune("copper_axe"))));
        // Tempest Axe (Woodcutting 20): the grid has 9 cells, so 4 breeze rods (the doc said 8).
        book.register(new CraftingRecipe("tempest_axe", woodcutting, 20, "tempest_axe", 1,
                List.of("BDB", "PAP", "BDB"), Map.of(
                        'B', Ingredient.vanilla(Material.BREEZE_ROD),
                        'D', Ingredient.vanilla(Material.DIAMOND_BLOCK),
                        'P', Ingredient.rune("abyssal_plate"),
                        'A', Ingredient.rune("breeze_axe"))));
    }
}
