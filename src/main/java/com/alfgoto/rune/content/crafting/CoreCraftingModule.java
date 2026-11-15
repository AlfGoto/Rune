package com.alfgoto.rune.content.crafting;

import com.alfgoto.rune.content.items.CoreItemsModule;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.crafting.CraftingModule;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;

/** Registers the core recipes. For now only a test recipe: 4 cobblestone → Test Item, at Mining level 2. */
@AutoService(RuneModule.class)
public final class CoreCraftingModule implements RuneModule {

    /** Mining level that unlocks the Components. Not decided in docs: 1 means no lock. */
    private static final int COMPONENT_LEVEL = 1;

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(CraftingModule.class, CoreItemsModule.class, CoreJobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RecipeBook book = ctx.get(RecipeBook.class);
        Job mining = ctx.get(JobRegistry.class).registry().get("mining");
        book.register(new CraftingRecipe(
                "test_item",
                mining,
                2,
                "test_item",
                1,
                List.of("CC", "CC"),
                Map.of('C', Ingredient.vanilla(Material.COBBLESTONE))));

        // The 7 Components (docs/jobs/mining.md). The ingredients are the documented ones;
        // the grid layout and the unlock level (COMPONENT_LEVEL) are not in docs yet.
        component(book, mining, "reinforced_handle", List.of("IL", "SS"), Map.of(
                'S', Ingredient.vanilla(Material.STICK),
                'I', Ingredient.vanilla(Material.IRON_INGOT),
                'L', Ingredient.vanilla(Material.LEATHER)));
        component(book, mining, "reinforced_lining", List.of("LW", "LI"), Map.of(
                'L', Ingredient.vanilla(Material.LEATHER),
                'W', Ingredient.vanilla(Material.WHITE_WOOL),
                'I', Ingredient.vanilla(Material.IRON_INGOT)));
        component(book, mining, "gem_setting", List.of(" N ", "NGN", " N "), Map.of(
                'G', Ingredient.vanilla(Material.GOLD_INGOT),
                'N', Ingredient.vanilla(Material.GOLD_NUGGET)));
        component(book, mining, "echo_lens", List.of("EAE", "ETE"), Map.of(
                'E', Ingredient.vanilla(Material.ECHO_SHARD),
                'A', Ingredient.vanilla(Material.AMETHYST_SHARD),
                'T', Ingredient.vanilla(Material.TINTED_GLASS)));
        component(book, mining, "deep_core", List.of("DAD", "AXA", "DAD"), Map.of(
                'D', Ingredient.vanilla(Material.DEEPSLATE),
                'A', Ingredient.vanilla(Material.AMETHYST_SHARD),
                'X', Ingredient.vanilla(Material.DIAMOND)));
        component(book, mining, "abyssal_plate", List.of("OCO", "CSC", "OCO"), Map.of(
                'O', Ingredient.vanilla(Material.OBSIDIAN),
                'C', Ingredient.vanilla(Material.CRYING_OBSIDIAN),
                'S', Ingredient.vanilla(Material.NETHERITE_SCRAP)));
        component(book, mining, "legendary_core", List.of("DND", "EHE", "DID"), Map.of(
                'N', Ingredient.vanilla(Material.NETHER_STAR),
                'H', Ingredient.vanilla(Material.DRAGON_HEAD),
                'I', Ingredient.vanilla(Material.NETHERITE_INGOT),
                'D', Ingredient.vanilla(Material.DIAMOND_BLOCK),
                'E', Ingredient.vanilla(Material.ECHO_SHARD)));
    }

    /** Components are shared by every Job; the recipe is attached to Mining, whose doc defines it. */
    private static void component(
            RecipeBook book, Job mining, String id, List<String> shape, Map<Character, Ingredient> ingredients) {
        book.register(new CraftingRecipe(id, mining, COMPONENT_LEVEL, id, 1, shape, ingredients));
    }
}
