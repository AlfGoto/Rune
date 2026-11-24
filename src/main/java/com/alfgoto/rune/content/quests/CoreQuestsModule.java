package com.alfgoto.rune.content.quests;

import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.content.places.CorePlacesModule;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.blocktracking.SaplingPlanters;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.trees.TreesModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.places.Place;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlacesModule;
import com.alfgoto.rune.gameplay.quests.QuestAction;
import com.alfgoto.rune.gameplay.quests.QuestObjective;
import com.alfgoto.rune.gameplay.quests.QuestType;
import com.alfgoto.rune.gameplay.quests.QuestTypeRegistry;
import com.alfgoto.rune.gameplay.quests.QuestsModule;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * Puts the Quest Types into the shared registry: the test type (Mining, base size 10) and the four Mining
 * Quest Types of {@code docs/jobs/mining.md}: Collect, Depth, Tool, Place. Each one only counts blocks
 * that gave Mining XP, so blocks placed by a player never count (see {@code BlockActionListener}).
 */
@AutoService(RuneModule.class)
public final class CoreQuestsModule implements RuneModule {

    public static final QuestType TEST = new QuestType("test", CoreJobs.MINING, 10);

    /** Collect: "mine 20 iron" (iron ore and deepslate iron ore). */
    static final Set<Material> COLLECT_BLOCKS = Set.of(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE);

    /** Depth: "mine 10 ores below Y -32" (the block must be at Y -33 or lower). */
    static final int DEPTH_BELOW_Y = -32;

    /** Tool: "mine 100 blocks with a Coal Pickaxe". */
    static final String TOOL_ITEM_ID = "coal_pickaxe";

    /** Place: "mine 50 blocks in a Mineshaft". */
    static final String PLACE_ID = "mineshaft";

    /** Woodcutting Place: "cut 40 logs in a Dark Forest". */
    static final String WOOD_PLACE_ID = "dark_forest";

    /** Woodcutting Tool: "fell 20 trees with a Jungle Axe". */
    static final String WOOD_TOOL_ID = "jungle_axe";

    @Override
    public void enable(RuneContext ctx) {
        QuestTypeRegistry types = ctx.get(QuestTypeRegistry.class);
        ItemRegistry items = ctx.get(ItemRegistry.class);
        PlaceDetector places = ctx.get(PlaceDetector.class);
        types.register(TEST);
        types.register(new QuestType("collect", CoreJobs.MINING, 20, event -> isCollectBlock(event.block())));
        types.register(new QuestType("depth", CoreJobs.MINING, 10, event -> isDeepOre(event.block())));
        types.register(new QuestType("tool", CoreJobs.MINING, 100, event -> isHeldItem(items, event.tool())));
        QuestObjective inPlace = event -> isInPlace(places, event);
        types.register(new QuestType("place", CoreJobs.MINING, 50, inPlace));
        registerWoodcutting(ctx, types, items, places);
    }

    /**
     * The five Woodcutting Quest Types of {@code docs/jobs/woodcutting.md}. Collect, Place and Variety count
     * natural logs only (blocks that gave full Woodcutting XP). Tool and Reforestation count trees.
     */
    private void registerWoodcutting(
            RuneContext ctx, QuestTypeRegistry types, ItemRegistry items, PlaceDetector places) {
        types.register(new QuestType(
                "wood_collect", CoreJobs.WOODCUTTING, 64, event -> isSpruceLog(event.block())));
        types.register(new QuestType(
                "wood_place",
                CoreJobs.WOODCUTTING,
                40,
                event -> event.block() != null
                        && WoodTypes.isLog(event.block().getType())
                        && isInPlace(places, event, WOOD_PLACE_ID)));
        // Only 12 wood types exist, so a high Tier cannot ask for more than that.
        types.register(new QuestType(
                        "wood_variety",
                        CoreJobs.WOODCUTTING,
                        5,
                        event -> event.block() != null && WoodTypes.isLog(event.block().getType()))
                .distinctBy(event -> WoodTypes.of(event.block().getType()))
                .cappedAt(WoodTypes.IDS.size()));
        types.register(new QuestType(
                        "wood_tool", CoreJobs.WOODCUTTING, 20, event -> isHeldItem(items, event.tool(), WOOD_TOOL_ID))
                .counting(QuestAction.TREE_FELLED));
        types.register(new QuestType("reforestation", CoreJobs.WOODCUTTING, 25).counting(QuestAction.TREE_GROWN));
        ctx.listen(new TreeQuestListener(
                ctx.plugin().getServer(), ctx.get(SaplingPlanters.class), ctx.get(BlockTracker.class)));
    }

    static boolean isSpruceLog(Block block) {
        return block != null && (block.getType() == Material.SPRUCE_LOG || block.getType() == Material.SPRUCE_WOOD);
    }

    /**
     * Place: the broken block OR the player is in a Mineshaft. A Mineshaft's pieces are only the tunnel
     * space, so the stone around a tunnel is outside every piece; the player standing in the tunnel
     * must count too.
     */
    static boolean isInPlace(PlaceDetector places, JobActionEvent event) {
        return isInPlace(places, event, PLACE_ID);
    }

    /** The broken block OR the player is inside the Place {@code placeId}. */
    static boolean isInPlace(PlaceDetector places, JobActionEvent event, String placeId) {
        if (event.block() != null && hasPlace(places.placesAt(event.block().getLocation()), placeId)) {
            return true;
        }
        return event.player() != null && hasPlace(places.placesOf(event.player()), placeId);
    }

    private static boolean hasPlace(List<Place> found, String placeId) {
        return found.stream().anyMatch(p -> p.id().equals(placeId));
    }

    static boolean isCollectBlock(Block block) {
        return block != null && COLLECT_BLOCKS.contains(block.getType());
    }

    static boolean isDeepOre(Block block) {
        return block != null && isOre(block.getType()) && block.getY() < DEPTH_BELOW_Y;
    }

    /** Ores: every vanilla "…_ore" block (incl. deepslate, nether gold, quartz) and ancient debris. */
    static boolean isOre(Material material) {
        return material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }

    static boolean isHeldItem(ItemRegistry items, ItemStack tool) {
        return isHeldItem(items, tool, TOOL_ITEM_ID);
    }

    static boolean isHeldItem(ItemRegistry items, ItemStack tool, String itemId) {
        return items.of(tool).map(RuneItem::id).filter(itemId::equals).isPresent();
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                QuestsModule.class,
                CoreJobsModule.class,
                ItemsModule.class,
                PlacesModule.class,
                CorePlacesModule.class,
                BlockTrackingModule.class,
                TreesModule.class);
    }
}
