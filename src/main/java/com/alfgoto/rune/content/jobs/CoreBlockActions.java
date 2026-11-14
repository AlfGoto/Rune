package com.alfgoto.rune.content.jobs;

import com.alfgoto.rune.gameplay.jobs.BlockAction;
import com.alfgoto.rune.gameplay.jobs.BlockActions;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * Block → Job Action definitions for Mining and Woodcutting, from {@code docs/jobs/mining.md} and
 * {@code docs/jobs/woodcutting.md}.
 *
 * <p>The block decides the Job, never the tool: {@link #jobIdOf} starts from the vanilla tool tags, with
 * carve-outs (axe crops → Farming, bee nests → Ranching) and additions (leaves → Woodcutting).
 * Jobs that do not exist yet (Excavation, Farming, Ranching) simply give nothing for now.
 * A block with no XP value in the docs gives no Job XP.
 */
public final class CoreBlockActions {

    /** Axe-tag blocks owned by Farming (crops). */
    static final Set<Material> FARMING_CARVE_OUTS = Set.of(
            Material.PUMPKIN,
            Material.CARVED_PUMPKIN,
            Material.JACK_O_LANTERN,
            Material.MELON,
            Material.COCOA,
            Material.BAMBOO,
            Material.BAMBOO_SAPLING);

    /** Axe-tag blocks owned by Ranching (bees). */
    static final Set<Material> RANCHING_CARVE_OUTS = Set.of(Material.BEE_NEST, Material.BEEHIVE);

    /** Blocks Woodcutting owns that are not in the vanilla axe tag (hoe blocks in vanilla). */
    static final Set<Material> WOODCUTTING_ADDITIONS = Set.of(
            Material.NETHER_WART_BLOCK, Material.WARPED_WART_BLOCK, Material.SHROOMLIGHT, Material.GLOW_LICHEN);

    private static final Map<Material, Double> XP = new HashMap<>();

    static {
        buildMiningXp();
        buildWoodcuttingXp();
    }

    private CoreBlockActions() {}

    /** The id of the Job that owns this block, if any. */
    public static Optional<String> jobIdOf(Material material) {
        if (Tag.LEAVES.isTagged(material) || WOODCUTTING_ADDITIONS.contains(material)) {
            return Optional.of("woodcutting");
        }
        if (Tag.MINEABLE_AXE.isTagged(material)) {
            if (FARMING_CARVE_OUTS.contains(material)) {
                return Optional.of("farming");
            }
            if (RANCHING_CARVE_OUTS.contains(material)) {
                return Optional.of("ranching");
            }
            return Optional.of("woodcutting");
        }
        if (Tag.MINEABLE_PICKAXE.isTagged(material)) {
            return Optional.of("mining");
        }
        if (Tag.MINEABLE_SHOVEL.isTagged(material)) {
            return Optional.of("excavation");
        }
        return Optional.empty();
    }

    /** Base Job XP of this block (before the anti-exploit rules), if the docs give one. */
    public static OptionalDouble baseXp(Material material) {
        Double xp = XP.get(material);
        return xp == null ? OptionalDouble.empty() : OptionalDouble.of(xp);
    }

    /** Registers every block with a Job in {@code jobs} and a base XP value. */
    public static void registerAll(BlockActions actions, JobRegistry jobs) {
        for (Map.Entry<Material, Double> entry : XP.entrySet()) {
            Material material = entry.getKey();
            Optional<String> jobId = jobIdOf(material);
            if (jobId.isEmpty() || !jobs.registry().contains(jobId.get())) {
                continue;
            }
            Job job = jobs.registry().get(jobId.get());
            actions.register(material, new BlockAction(job, entry.getValue()));
        }
    }

    private static void xp(double xp, String... names) {
        for (String name : names) {
            Material material = Material.matchMaterial(name);
            if (material != null) {
                XP.put(material, xp);
            }
        }
    }

    private static void buildMiningXp() {
        // Filler blocks follow their hardness.
        xp(0.25, "NETHERRACK");
        xp(1, "STONE", "GRANITE", "DIORITE", "ANDESITE", "TUFF", "COBBLESTONE", "BASALT");
        xp(1.5, "DEEPSLATE", "COBBLED_DEEPSLATE");
        xp(2, "END_STONE");
        // Fixed values.
        xp(4, "COAL_ORE", "COPPER_ORE", "NETHER_GOLD_ORE");
        xp(5, "NETHER_QUARTZ_ORE", "AMETHYST_CLUSTER");
        xp(6, "OBSIDIAN");
        xp(8, "IRON_ORE", "REDSTONE_ORE", "LAPIS_ORE");
        xp(12, "GOLD_ORE");
        xp(15, "GILDED_BLACKSTONE");
        xp(20, "RAW_IRON_BLOCK", "RAW_COPPER_BLOCK", "RAW_GOLD_BLOCK");
        xp(30, "DIAMOND_ORE");
        xp(40, "EMERALD_ORE");
        xp(60, "ANCIENT_DEBRIS");
        xp(100, "GOLD_BLOCK");
        // Deepslate ore variants: ×1.25.
        deepslateOre("COAL_ORE", 4);
        deepslateOre("COPPER_ORE", 4);
        deepslateOre("IRON_ORE", 8);
        deepslateOre("REDSTONE_ORE", 8);
        deepslateOre("LAPIS_ORE", 8);
        deepslateOre("GOLD_ORE", 12);
        deepslateOre("DIAMOND_ORE", 30);
        deepslateOre("EMERALD_ORE", 40);
        // Copper blocks (Trial Chambers, all variants): 6.
        List<String> weathering = List.of("", "EXPOSED_", "WEATHERED_", "OXIDIZED_");
        List<String> bases = List.of(
                "COPPER_BLOCK", "CUT_COPPER", "CUT_COPPER_STAIRS", "CUT_COPPER_SLAB", "CHISELED_COPPER",
                "COPPER_GRATE", "COPPER_BULB", "COPPER_DOOR", "COPPER_TRAPDOOR");
        for (String wear : weathering) {
            for (String base : bases) {
                xp(6, wear + base, "WAXED_" + wear + base);
            }
        }
    }

    private static void deepslateOre(String ore, double baseXp) {
        xp(baseXp * 1.25, "DEEPSLATE_" + ore);
    }

    private static void buildWoodcuttingXp() {
        xp(0.05, "VINE");
        xp(0.1, "NETHER_WART_BLOCK", "WARPED_WART_BLOCK", "GLOW_LICHEN");
        for (Material material : Material.values()) {
            if (Tag.LEAVES.isTagged(material)) {
                XP.put(material, 0.1);
            }
            if (Tag.PLANKS.isTagged(material)) {
                XP.put(material, 0.5); // structure planks (villages, mineshafts…)
            }
        }
        xp(0.5, "BIG_DRIPLEAF", "BIG_DRIPLEAF_STEM", "MANGROVE_ROOTS");
        xp(1, "RED_MUSHROOM_BLOCK", "BROWN_MUSHROOM_BLOCK", "SHROOMLIGHT");
        xp(2, "MUSHROOM_STEM", "CHORUS_PLANT");
        xp(4, "CHORUS_FLOWER");
        xp(20, "CREAKING_HEART");
        // Logs, wood, stripped logs and stripped wood give the same XP as their log.
        for (String wood : List.of("OAK", "BIRCH", "SPRUCE", "ACACIA", "JUNGLE", "DARK_OAK", "MANGROVE", "POPLAR")) {
            logs(3, wood + "_LOG", wood + "_WOOD");
        }
        logs(4, "CHERRY_LOG", "CHERRY_WOOD");
        logs(5, "PALE_OAK_LOG", "PALE_OAK_WOOD");
        logs(4, "CRIMSON_STEM", "CRIMSON_HYPHAE");
        logs(4, "WARPED_STEM", "WARPED_HYPHAE");
    }

    private static void logs(double xp, String log, String wood) {
        xp(xp, log, wood, "STRIPPED_" + log, "STRIPPED_" + wood);
    }
}
