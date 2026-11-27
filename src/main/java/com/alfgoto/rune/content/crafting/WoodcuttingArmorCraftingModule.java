package com.alfgoto.rune.content.crafting;

import com.alfgoto.rune.content.items.CoreItemsModule;
import com.alfgoto.rune.content.items.WoodcuttingArmorModule;
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

/**
 * Recipes of the Common (Woodcutting 3) and Uncommon (Woodcutting 7) Woodcutting armor. The ingredients are the
 * documented ones (docs/jobs/woodcutting.md); the grid layouts are not in docs. Each Uncommon piece consumes its
 * Common piece (an Upgrade) and adds a Reinforced Lining.
 */
@AutoService(RuneModule.class)
public final class WoodcuttingArmorCraftingModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                CraftingModule.class, CoreItemsModule.class, WoodcuttingArmorModule.class, CoreJobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        RecipeBook book = ctx.get(RecipeBook.class);
        Job woodcutting = ctx.get(JobRegistry.class).registry().get("woodcutting");
        Ingredient leather = Ingredient.vanilla(Material.LEATHER);
        Ingredient iron = Ingredient.vanilla(Material.IRON_INGOT);
        Ingredient lining = Ingredient.rune("reinforced_lining");

        // Common: 4 leather + 1 oak leaves / 6 leather + 2 stripped oak logs / 5 leather + 2 string /
        // 4 leather + 2 feathers.
        recipe(book, woodcutting, 3, "lumberjacks_cap", List.of("LOL", "L L"), Map.of(
                'L', leather, 'O', Ingredient.vanilla(Material.OAK_LEAVES)));
        recipe(book, woodcutting, 3, "lumberjacks_vest", List.of("L L", "SLS", "LLL"), Map.of(
                'L', leather, 'S', Ingredient.vanilla(Material.STRIPPED_OAK_LOG)));
        recipe(book, woodcutting, 3, "lumberjacks_trousers", List.of("LLL", "L L", "S S"), Map.of(
                'L', leather, 'S', Ingredient.vanilla(Material.STRING)));
        recipe(book, woodcutting, 3, "lumberjacks_boots", List.of("L L", "L L", "F F"), Map.of(
                'L', leather, 'F', Ingredient.vanilla(Material.FEATHER)));

        // Uncommon: the Common piece + a Reinforced Lining + 2 iron + shears / 4 iron / 3 iron + rabbit's foot /
        // 2 iron + slime block.
        recipe(book, woodcutting, 7, "fellers_hood", List.of("IHI", "SR "), Map.of(
                'I', iron, 'H', Ingredient.rune("lumberjacks_cap"), 'S', Ingredient.vanilla(Material.SHEARS),
                'R', lining));
        recipe(book, woodcutting, 7, "fellers_jerkin", List.of("IVI", "IRI"), Map.of(
                'I', iron, 'V', Ingredient.rune("lumberjacks_vest"), 'R', lining));
        recipe(book, woodcutting, 7, "fellers_leggings", List.of("ITI", "RFI"), Map.of(
                'I', iron, 'T', Ingredient.rune("lumberjacks_trousers"),
                'F', Ingredient.vanilla(Material.RABBIT_FOOT), 'R', lining));
        recipe(book, woodcutting, 7, "fellers_boots", List.of("IBI", "SR "), Map.of(
                'I', iron, 'B', Ingredient.rune("lumberjacks_boots"), 'S', Ingredient.vanilla(Material.SLIME_BLOCK),
                'R', lining));
    }

    private static void recipe(
            RecipeBook book, Job job, int level, String id, List<String> shape, Map<Character, Ingredient> ing) {
        book.register(new CraftingRecipe(id, job, level, id, 1, shape, ing));
    }
}
