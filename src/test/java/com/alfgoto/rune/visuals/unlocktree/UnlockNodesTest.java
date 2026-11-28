package com.alfgoto.rune.visuals.unlocktree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.foundations.config.BlueprintConfig;
import com.alfgoto.rune.gameplay.blueprints.Blueprint;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class UnlockNodesTest {

    private final Job mining = Job.builder("mining").build();
    private final Job woodcutting = Job.builder("woodcutting").build();

    private static RuneItem item(String id) {
        return new RuneItem(id, Rarity.COMMON, Material.PAPER, id);
    }

    private CraftingRecipe recipe(String id, Job job, int level) {
        return new CraftingRecipe(
                id, job, level, id, 1, List.of("S"), Map.of('S', Ingredient.vanilla(Material.STONE)));
    }

    private List<UnlockNode> build(Job job, List<CraftingRecipe> recipes, int maxLevel) {
        return UnlockNodes.build(
                job,
                recipes,
                List.of(
                        new BlueprintConfig.Milestone("mining", 22, "briseroc"),
                        new BlueprintConfig.Milestone("mining", 30, "not_registered"),
                        new BlueprintConfig.Milestone("woodcutting", 22, "elder_axe")),
                id -> Set.of("briseroc", "elder_axe").contains(id) ? Optional.of(new Blueprint(id)) : Optional.empty(),
                level -> switch (level) {
                    case 5 -> Optional.of(Rarity.COMMON);
                    case 10 -> Optional.of(Rarity.UNCOMMON);
                    case 25 -> Optional.of(Rarity.EPIC);
                    default -> Optional.empty();
                },
                UnlockNodesTest::item,
                maxLevel);
    }

    @Test
    void nodesAreSortedByLevelAndLevelsWithNoUnlockGetNoNode() {
        List<UnlockNode> nodes = build(mining, List.of(recipe("bone_pickaxe", mining, 7), recipe("flint", mining, 2)), 50);
        assertEquals(List.of(2, 5, 7, 10, 22, 25), nodes.stream().map(UnlockNode::level).toList());
    }

    @Test
    void oneNodeHoldsEverythingAtItsLevelAndTheStrongestKindGivesTheFrame() {
        List<UnlockNode> nodes = build(mining, List.of(recipe("a", mining, 5), recipe("b", mining, 22)), 50);
        UnlockNode five = nodes.stream().filter(n -> n.level() == 5).findFirst().orElseThrow();
        assertEquals(2, five.unlocks().size());
        assertEquals(Unlock.Kind.CACHE, five.main().kind());
        assertEquals("goal", five.main().kind().frame());
        UnlockNode tw = nodes.stream().filter(n -> n.level() == 22).findFirst().orElseThrow();
        assertEquals(Unlock.Kind.BLUEPRINT, tw.main().kind());
        assertEquals("challenge", tw.main().kind().frame());
    }

    @Test
    void aRecipeAloneIsATask() {
        UnlockNode node = build(mining, List.of(recipe("flint", mining, 2)), 1).get(0);
        assertEquals("task", node.main().kind().frame());
        assertEquals("flint", node.main().icon().id());
    }

    @Test
    void recipesAtLevelOneAreStarterRecipesNotUnlocks() {
        Job other = Job.builder("other").build();
        assertTrue(build(other, List.of(recipe("component", other, 1)), 1).isEmpty());
    }

    @Test
    void onlyThisJobsRecipesAndBlueprintsCount() {
        List<UnlockNode> nodes = build(woodcutting, List.of(recipe("flint", mining, 2)), 1);
        assertEquals(List.of(22), nodes.stream().map(UnlockNode::level).toList());
    }

    @Test
    void aBlueprintRecipeIsNotAnUnlockOfItsLevelAndAnUnregisteredBlueprintIsSkipped() {
        CraftingRecipe blueprintRecipe =
                CraftingRecipe.blueprint("briseroc", mining, "briseroc", 1, List.of("S"), Map.of('S', Ingredient.vanilla(Material.STONE)));
        List<UnlockNode> nodes = build(mining, List.of(blueprintRecipe), 1);
        assertEquals(List.of(22), nodes.stream().map(UnlockNode::level).toList());
        assertFalse(nodes.stream().anyMatch(n -> n.level() == 30));
    }

    @Test
    void milestoneCachesStopAtTheMaxLevel() {
        List<UnlockNode> nodes = build(mining, List.of(), 9);
        assertEquals(List.of(5, 22), nodes.stream().map(UnlockNode::level).toList());
    }

    @Test
    void nodeJsonHasTheFrameTheParentAndNoChatAnnouncement() {
        UnlockNode node = build(mining, List.of(), 50).get(0);
        String json = TreeJson.node(node, TreeJson.jobPath(mining));
        assertTrue(json.contains("\"frame\":\"goal\""));
        assertTrue(json.contains("\"parent\":\"rune:unlock/mining\""));
        assertTrue(json.contains("\"announce_to_chat\":false"));
        assertTrue(json.contains("\"show_toast\":true"));
        assertTrue(json.contains("rune.unlock_tree.node"));
        assertTrue(json.contains("minecraft:impossible"));
    }
}
