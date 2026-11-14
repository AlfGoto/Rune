package com.alfgoto.rune.content.jobs;

import com.alfgoto.rune.gameplay.jobs.BlockAction;
import com.alfgoto.rune.gameplay.jobs.BlockActions;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * Block → Job mapping for Mining and Woodcutting, from {@code docs/jobs/mining.md} and
 * {@code docs/jobs/woodcutting.md}. The XP per block lives in the config ({@code jobs.block-xp}).
 *
 * <p>The block decides the Job, never the tool: {@link #jobIdOf} starts from the vanilla tool tags, with
 * carve-outs (axe crops → Farming, bee nests → Ranching) and additions (leaves → Woodcutting).
 * Jobs that do not exist yet (Excavation, Farming, Ranching) simply give nothing for now.
 * A block with no XP value in the config gives no Job XP.
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

    /** Registers every block that has a Job in {@code jobs} and a base XP value in {@code blockXp}. */
    public static void registerAll(BlockActions actions, JobRegistry jobs, Map<Material, Double> blockXp) {
        for (Map.Entry<Material, Double> entry : blockXp.entrySet()) {
            Material material = entry.getKey();
            Optional<String> jobId = jobIdOf(material);
            if (jobId.isEmpty() || !jobs.registry().contains(jobId.get())) {
                continue;
            }
            Job job = jobs.registry().get(jobId.get());
            actions.register(material, new BlockAction(job, entry.getValue()));
        }
    }
}
