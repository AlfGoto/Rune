package com.alfgoto.rune.visuals.unlocktree;

import com.alfgoto.rune.foundations.config.BlueprintConfig;
import com.alfgoto.rune.gameplay.blueprints.Blueprint;
import com.alfgoto.rune.gameplay.blueprints.Blueprints;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.kyori.adventure.text.Component;

/**
 * Builds the nodes of one Job's line from its unlock data: recipes (by Job Level), Blueprint milestones
 * (config) and Milestone Caches. Plain data in, plain data out: no server needed.
 */
public final class UnlockNodes {

    /** A recipe at level 1 is known from the start (Starter Recipe, Components): it is not an unlock. */
    static final int FIRST_UNLOCK_LEVEL = 2;

    private UnlockNodes() {}

    /**
     * The nodes of {@code job}, lowest level first. A level that unlocks nothing gets no node.
     *
     * @param recipes every recipe; Blueprint recipes are skipped (they unlock by their Blueprint)
     * @param blueprintMilestones the config milestones, of every Job
     * @param blueprints finds a registered Blueprint; one that is not registered is skipped
     * @param cacheMilestone the Rarity of the Milestone Cache of a Job Level, if any
     * @param items finds an item by id, for icons
     * @param maxLevel the last Job Level checked for Milestone Caches (they never end)
     */
    public static List<UnlockNode> build(
            Job job,
            Collection<CraftingRecipe> recipes,
            List<BlueprintConfig.Milestone> blueprintMilestones,
            Function<String, Optional<Blueprint>> blueprints,
            IntFunction<Optional<Rarity>> cacheMilestone,
            Function<String, RuneItem> items,
            int maxLevel) {
        Map<Integer, List<Unlock>> byLevel = new TreeMap<>();
        for (CraftingRecipe recipe : recipes) {
            if (recipe.job().equals(job) && !recipe.blueprint() && recipe.level() >= FIRST_UNLOCK_LEVEL) {
                RuneItem result = items.apply(recipe.result());
                byLevel.computeIfAbsent(recipe.level(), l -> new ArrayList<>())
                        .add(new Unlock(
                                Unlock.Kind.RECIPE,
                                Component.translatable(result.translationKey(), result.fallbackName()),
                                result));
            }
        }
        for (BlueprintConfig.Milestone milestone : blueprintMilestones) {
            Blueprint blueprint = milestone.job().equals(job.id())
                    ? blueprints.apply(milestone.blueprint()).orElse(null)
                    : null;
            if (blueprint != null && milestone.level() >= FIRST_UNLOCK_LEVEL) {
                byLevel.computeIfAbsent(milestone.level(), l -> new ArrayList<>())
                        .add(new Unlock(
                                Unlock.Kind.BLUEPRINT,
                                Component.translatable(blueprint.translationKey(), blueprint.fallbackName()),
                                items.apply(Blueprints.ITEM_ID)));
            }
        }
        for (int level = FIRST_UNLOCK_LEVEL; level <= maxLevel; level++) {
            Rarity rarity = cacheMilestone.apply(level).orElse(null);
            if (rarity != null) {
                RuneItem cache = items.apply(Caches.itemId(rarity));
                byLevel.computeIfAbsent(level, l -> new ArrayList<>())
                        .add(new Unlock(Unlock.Kind.CACHE, Text.rarityName(rarity).color(rarity.color()), cache));
            }
        }
        List<UnlockNode> nodes = new ArrayList<>();
        byLevel.forEach((level, unlocks) -> nodes.add(new UnlockNode(job, level, unlocks)));
        return nodes;
    }
}
