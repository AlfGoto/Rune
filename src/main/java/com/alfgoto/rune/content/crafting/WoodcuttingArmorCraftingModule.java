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
 * Recipes of the Common (Woodcutting 3), Uncommon (7), Rare (12) and Epic (18) Woodcutting armor. The ingredients are the
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

        Ingredient diamond = Ingredient.vanilla(Material.DIAMOND);
        Ingredient setting = Ingredient.rune("gem_setting");
        Ingredient plate = Ingredient.rune("abyssal_plate");
        Ingredient block = Ingredient.vanilla(Material.DIAMOND_BLOCK);

        // Rare: the Uncommon piece + a Gem Setting + 2 diamonds + spyglass / 3 diamonds + 2 resin blocks /
        // 2 diamonds + 2 breeze rods / 2 diamonds + rabbit's foot.
        recipe(book, woodcutting, 12, "sylvan_helm", List.of("DHD", "SG "), Map.of(
                'D', diamond, 'H', Ingredient.rune("fellers_hood"), 'S', Ingredient.vanilla(Material.SPYGLASS),
                'G', setting));
        recipe(book, woodcutting, 12, "sylvan_cuirass", List.of("DJD", "RGR", " D "), Map.of(
                'D', diamond, 'J', Ingredient.rune("fellers_jerkin"),
                'R', Ingredient.vanilla(Material.RESIN_BLOCK), 'G', setting));
        recipe(book, woodcutting, 12, "sylvan_leggings", List.of("DLD", "BGB"), Map.of(
                'D', diamond, 'L', Ingredient.rune("fellers_leggings"),
                'B', Ingredient.vanilla(Material.BREEZE_ROD), 'G', setting));
        recipe(book, woodcutting, 12, "sylvan_boots", List.of("DBD", "FG "), Map.of(
                'D', diamond, 'B', Ingredient.rune("fellers_boots"),
                'F', Ingredient.vanilla(Material.RABBIT_FOOT), 'G', setting));

        // Epic: the Rare piece + Abyssal Plate(s) + 2 diamond blocks + the extra ingredient.
        recipe(book, woodcutting, 18, "heartwood_helm", List.of("XHX", "PC "), Map.of(
                'X', block, 'H', Ingredient.rune("sylvan_helm"), 'P', plate,
                'C', Ingredient.vanilla(Material.CREAKING_HEART)));
        recipe(book, woodcutting, 18, "heartwood_cuirass", List.of("XVX", "PTP"), Map.of(
                'X', block, 'V', Ingredient.rune("sylvan_cuirass"), 'P', plate,
                'T', Ingredient.vanilla(Material.TOTEM_OF_UNDYING)));
        recipe(book, woodcutting, 18, "heartwood_leggings", List.of("XLX", "PK "), Map.of(
                'X', block, 'L', Ingredient.rune("sylvan_leggings"), 'P', plate,
                'K', Ingredient.vanilla(Material.OMINOUS_TRIAL_KEY)));
        recipe(book, woodcutting, 18, "heartwood_boots", List.of("XBX", "PMM"), Map.of(
                'X', block, 'B', Ingredient.rune("sylvan_boots"), 'P', plate,
                'M', Ingredient.vanilla(Material.PHANTOM_MEMBRANE)));
    }

    private static void recipe(
            RecipeBook book, Job job, int level, String id, List<String> shape, Map<Character, Ingredient> ing) {
        book.register(new CraftingRecipe(id, job, level, id, 1, shape, ing));
    }
}
