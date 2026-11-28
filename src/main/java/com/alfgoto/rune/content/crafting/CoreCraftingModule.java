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

        // Flint and Coal pickaxes: first Rune pickaxes, unlocked at Mining 2 (docs/jobs/mining.md).
        // Ingredients are the documented ones; the grid layout is proposed (not in docs).
        book.register(new CraftingRecipe("flint_pickaxe", mining, 2, "flint_pickaxe", 1,
                List.of("FFF", " S ", " S "), Map.of(
                        'F', Ingredient.vanilla(Material.FLINT),
                        'S', Ingredient.vanilla(Material.STICK))));
        book.register(new CraftingRecipe("coal_pickaxe", mining, 2, "coal_pickaxe", 1,
                List.of("CCB", " S ", " S "), Map.of(
                        'C', Ingredient.vanilla(Material.COAL),
                        'B', Ingredient.vanilla(Material.COBBLESTONE),
                        'S', Ingredient.vanilla(Material.STICK))));

        biomePickaxes(book, mining);

        // Ironbound (Mining 5): an Upgrade of the Flint Pickaxe, so its Runes carry over. Layout proposed.
        book.register(new CraftingRecipe("ironbound_pickaxe", mining, 5, "ironbound_pickaxe", 1,
                List.of("III", "HP "), Map.of(
                        'I', Ingredient.vanilla(Material.IRON_INGOT),
                        'H', Ingredient.rune("reinforced_handle"),
                        'P', Ingredient.rune("flint_pickaxe"))));

        // Bone (Mining 7) and Echo (Mining 15). Ingredients are the documented ones; the grid layouts are proposed.
        book.register(new CraftingRecipe("bone_pickaxe", mining, 7, "bone_pickaxe", 1,
                List.of("BBB", " K ", " H "), Map.of(
                        'B', Ingredient.vanilla(Material.BONE_BLOCK),
                        'K', Ingredient.vanilla(Material.SKELETON_SKULL),
                        'H', Ingredient.rune("reinforced_handle"))));
        book.register(new CraftingRecipe("echo_pickaxe", mining, 15, "echo_pickaxe", 1,
                List.of("DLD", " P "), Map.of(
                        'D', Ingredient.vanilla(Material.DIAMOND),
                        'L', Ingredient.rune("echo_lens"),
                        'P', Ingredient.rune("bone_pickaxe"))));

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

        miningArmor(book, mining);
        woodcuttingAxes(ctx, book, ctx.get(JobRegistry.class).registry().get("woodcutting"));
        startAxes(book, ctx.get(JobRegistry.class).registry().get("woodcutting"));
    }

    /**
     * Flint Hatchet and Stone Hatchet (Woodcutting 2) and Woodsman's Axe (5). The ingredients are the documented
     * ones; the grid layouts are proposed (not in docs). Woodsman's consumes the Flint Hatchet (an Upgrade), so its
     * Runes carry over. The Copper Axe recipe is in SpeedToolsModule, next to its item.
     */
    private static void startAxes(RecipeBook book, Job woodcutting) {
        Ingredient stick = Ingredient.vanilla(Material.STICK);
        Ingredient handle = Ingredient.rune("reinforced_handle");
        book.register(new CraftingRecipe("flint_hatchet", woodcutting, 2, "flint_hatchet", 1,
                List.of("FF", "FS", " S"), Map.of('F', Ingredient.vanilla(Material.FLINT), 'S', stick)));
        book.register(new CraftingRecipe("stone_hatchet", woodcutting, 2, "stone_hatchet", 1,
                List.of("CF", "CS", " S"), Map.of(
                        'C', Ingredient.vanilla(Material.COBBLESTONE),
                        'F', Ingredient.vanilla(Material.FLINT),
                        'S', stick)));
        book.register(new CraftingRecipe("woodsmans_axe", woodcutting, 5, "woodsmans_axe", 1,
                List.of("III", "HA "), Map.of(
                        'I', Ingredient.vanilla(Material.IRON_INGOT),
                        'H', handle,
                        'A', Ingredient.rune("flint_hatchet"))));
    }

    /**
     * The Birch (Woodcutting 6), Spruce (6), Poplar (7) and Jungle (10) axes. The ingredients are the documented
     * ones; the grid layouts are proposed (not in docs).
     */
    private static void woodcuttingAxes(RuneContext ctx, RecipeBook book, Job woodcutting) {
        Ingredient handle = Ingredient.rune("reinforced_handle");
        book.register(new CraftingRecipe("birch_axe", woodcutting, 6, "birch_axe", 1,
                List.of("LLF", "LH ", " L "), Map.of(
                        'L', Ingredient.vanilla(Material.BIRCH_LOG),
                        'F', Ingredient.vanilla(Material.FEATHER),
                        'H', handle)));
        book.register(new CraftingRecipe("spruce_axe", woodcutting, 6, "spruce_axe", 1,
                List.of("LLB", "LH ", " L "), Map.of(
                        'L', Ingredient.vanilla(Material.SPRUCE_LOG),
                        'B', Ingredient.vanilla(Material.POWDER_SNOW_BUCKET),
                        'H', handle)));
        book.register(new CraftingRecipe("poplar_axe", woodcutting, 7, "poplar_axe", 1,
                List.of("LRO", "LHY", "LL "), Map.of(
                        'L', Ingredient.vanilla(Material.POPLAR_LOG),
                        'R', Ingredient.vanilla(Material.RED_POPLAR_LEAVES),
                        'O', Ingredient.vanilla(Material.ORANGE_POPLAR_LEAVES),
                        'Y', Ingredient.vanilla(Material.YELLOW_POPLAR_LEAVES),
                        'H', handle)));
        book.register(new CraftingRecipe("jungle_axe", woodcutting, 10, "jungle_axe", 1,
                List.of("LLC", "LGD", "LC "), Map.of(
                        'L', Ingredient.vanilla(Material.JUNGLE_LOG),
                        'C', Ingredient.vanilla(Material.COCOA_BEANS),
                        'D', Ingredient.vanilla(Material.DIAMOND),
                        'G', Ingredient.rune("gem_setting"))));
        essenceAxes(book, woodcutting);
    }

    /**
     * The Mangrove (Woodcutting 14), Pale Oak (15) and Chorus (18) axes. The ingredients are the documented
     * ones; the grid layouts are proposed (not in docs).
     */
    private static void essenceAxes(RecipeBook book, Job woodcutting) {
        Ingredient gem = Ingredient.rune("gem_setting");
        Ingredient diamond = Ingredient.vanilla(Material.DIAMOND);
        book.register(new CraftingRecipe("mangrove_axe", woodcutting, 14, "mangrove_axe", 1,
                List.of("LLP", "LGD", "LP "), Map.of(
                        'L', Ingredient.vanilla(Material.MANGROVE_LOG),
                        'P', Ingredient.vanilla(Material.MANGROVE_PROPAGULE),
                        'D', diamond,
                        'G', gem)));
        book.register(new CraftingRecipe("pale_oak_axe", woodcutting, 15, "pale_oak_axe", 1,
                List.of("LLH", "LGD", "L  "), Map.of(
                        'L', Ingredient.vanilla(Material.PALE_OAK_LOG),
                        'H', Ingredient.vanilla(Material.CREAKING_HEART),
                        'D', diamond,
                        'G', gem)));
        book.register(new CraftingRecipe("chorus_axe", woodcutting, 18, "chorus_axe", 1,
                List.of("FSF", "DPD", "FSF"), Map.of(
                        'F', Ingredient.vanilla(Material.CHORUS_FLOWER),
                        'S', Ingredient.vanilla(Material.SHULKER_SHELL),
                        'D', Ingredient.vanilla(Material.DIAMOND_BLOCK),
                        'P', Ingredient.rune("abyssal_plate"))));
    }

    /**
     * Lichen (Mining 6), Fiery (10), Emerald (13) and Prismarine (14). The ingredients and levels are the
     * documented ones; the grid layouts are in docs/jobs/mining.md.
     */
    private static void biomePickaxes(RecipeBook book, Job mining) {
        Ingredient handle = Ingredient.rune("reinforced_handle");
        Ingredient setting = Ingredient.rune("gem_setting");
        Ingredient diamond = Ingredient.vanilla(Material.DIAMOND);

        book.register(new CraftingRecipe("lichen_pickaxe", mining, 6, "lichen_pickaxe", 1,
                List.of("MLM", "BHB"), Map.of(
                        'M', Ingredient.vanilla(Material.MOSS_BLOCK),
                        'L', Ingredient.vanilla(Material.GLOW_LICHEN),
                        'B', Ingredient.vanilla(Material.GLOW_BERRIES),
                        'H', handle)));
        book.register(new CraftingRecipe("fiery_pickaxe", mining, 10, "fiery_pickaxe", 1,
                List.of("MMM", "RDR", " G "), Map.of(
                        'M', Ingredient.vanilla(Material.MAGMA_BLOCK),
                        'R', Ingredient.vanilla(Material.BLAZE_ROD),
                        'D', diamond,
                        'G', setting)));
        book.register(new CraftingRecipe("emerald_pickaxe", mining, 13, "emerald_pickaxe", 1,
                List.of("EEE", "HDG"), Map.of(
                        'E', Ingredient.vanilla(Material.EMERALD),
                        'H', Ingredient.vanilla(Material.GOAT_HORN),
                        'D', diamond,
                        'G', setting)));
        book.register(new CraftingRecipe("prismarine_pickaxe", mining, 14, "prismarine_pickaxe", 1,
                List.of("PNP", "DGD"), Map.of(
                        'P', Ingredient.vanilla(Material.PRISMARINE),
                        'N', Ingredient.vanilla(Material.NAUTILUS_SHELL),
                        'D', diamond,
                        'G', setting)));
    }

    /**
     * Common (Mining 3) and Uncommon (Mining 7) Mining armor. The ingredients are the documented ones; the grid
     * layouts are not in docs. Each Uncommon piece consumes its Common piece (an Upgrade).
     */
    private static void miningArmor(RecipeBook book, Job mining) {
        Ingredient leather = Ingredient.vanilla(Material.LEATHER);
        Ingredient copper = Ingredient.vanilla(Material.COPPER_INGOT);
        Ingredient iron = Ingredient.vanilla(Material.IRON_INGOT);
        Ingredient lining = Ingredient.rune("reinforced_lining");

        armor(book, mining, 3, "miners_helmet", List.of(" C ", "LLL", "L  "), Map.of(
                'L', leather, 'C', Ingredient.vanilla(Material.CANDLE)));
        armor(book, mining, 3, "miners_vest", List.of("LCL", "L L", "LCL"), Map.of('L', leather, 'C', copper));
        armor(book, mining, 3, "miners_trousers", List.of("LLL", "S S", "L L"), Map.of(
                'L', leather, 'S', Ingredient.vanilla(Material.STRING)));
        armor(book, mining, 3, "miners_boots", List.of("L L", "LWL"), Map.of(
                'L', leather, 'W', Ingredient.vanilla(Material.WHITE_WOOL)));

        armor(book, mining, 7, "cavers_helmet", List.of("CLC", "CHR"), Map.of(
                'C', copper, 'L', Ingredient.vanilla(Material.LANTERN),
                'H', Ingredient.rune("miners_helmet"), 'R', lining));
        armor(book, mining, 7, "reinforced_chestplate", List.of("IVI", "IRI"), Map.of(
                'I', iron, 'V', Ingredient.rune("miners_vest"), 'R', lining));
        armor(book, mining, 7, "cavers_leggings", List.of("IPI", "TRI"), Map.of(
                'I', iron, 'P', Ingredient.vanilla(Material.PISTON),
                'T', Ingredient.rune("miners_trousers"), 'R', lining));
        armor(book, mining, 7, "cavers_boots", List.of("IBI", "SR "), Map.of(
                'I', iron, 'B', Ingredient.rune("miners_boots"),
                'S', Ingredient.vanilla(Material.SLIME_BLOCK), 'R', lining));

        // Rare (Mining 12) and Epic (Mining 18): each piece consumes the previous one (an Upgrade).
        Ingredient diamond = Ingredient.vanilla(Material.DIAMOND);
        Ingredient diamondBlock = Ingredient.vanilla(Material.DIAMOND_BLOCK);
        Ingredient gem = Ingredient.rune("gem_setting");
        Ingredient plate = Ingredient.rune("abyssal_plate");
        Ingredient deepCore = Ingredient.rune("deep_core");
        Ingredient membrane = Ingredient.vanilla(Material.PHANTOM_MEMBRANE);

        armor(book, mining, 12, "drillers_helm", List.of("DSD", "HG "), Map.of(
                'D', diamond, 'S', Ingredient.vanilla(Material.SOUL_LANTERN),
                'H', Ingredient.rune("cavers_helmet"), 'G', gem));
        armor(book, mining, 12, "drillers_cuirass", List.of("DDD", "CSG"), Map.of(
                'D', diamond, 'C', Ingredient.rune("reinforced_chestplate"),
                'S', Ingredient.vanilla(Material.SHIELD), 'G', gem));
        armor(book, mining, 12, "drillers_leggings", List.of("DRD", "LRG"), Map.of(
                'D', diamond, 'R', Ingredient.vanilla(Material.REDSTONE_BLOCK),
                'L', Ingredient.rune("cavers_leggings"), 'G', gem));
        armor(book, mining, 12, "drillers_boots", List.of("DFD", "BFP", "G  "), Map.of(
                'D', diamond, 'F', Ingredient.vanilla(Material.FEATHER),
                'B', Ingredient.rune("cavers_boots"), 'P', membrane, 'G', gem));

        armor(book, mining, 18, "abyssal_helm", List.of("DSD", "HPE"), Map.of(
                'D', diamondBlock, 'S', Ingredient.vanilla(Material.SEA_LANTERN),
                'H', Ingredient.rune("drillers_helm"), 'P', plate, 'E', Ingredient.rune("echo_lens")));
        armor(book, mining, 18, "abyssal_cuirass", List.of("DPD", "CPK"), Map.of(
                'D', diamondBlock, 'P', plate,
                'C', Ingredient.rune("drillers_cuirass"), 'K', deepCore));
        armor(book, mining, 18, "abyssal_leggings", List.of("DPD", "LK "), Map.of(
                'D', diamondBlock, 'P', plate,
                'L', Ingredient.rune("drillers_leggings"), 'K', deepCore));
        armor(book, mining, 18, "abyssal_boots", List.of("DPD", "BMM"), Map.of(
                'D', diamondBlock, 'P', plate,
                'B', Ingredient.rune("drillers_boots"), 'M', membrane));
    }

    private static void armor(
            RecipeBook book, Job mining, int level, String id, List<String> shape, Map<Character, Ingredient> ing) {
        book.register(new CraftingRecipe(id, mining, level, id, 1, shape, ing));
    }

    /** Components are shared by every Job; the recipe is attached to Mining, whose doc defines it. */
    private static void component(
            RecipeBook book, Job mining, String id, List<String> shape, Map<Character, Ingredient> ingredients) {
        book.register(new CraftingRecipe(id, mining, COMPONENT_LEVEL, id, 1, shape, ingredients));
    }
}
