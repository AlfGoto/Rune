package com.alfgoto.rune.content.quests;

import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.content.places.CorePlacesModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.places.Place;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.places.PlacesModule;
import com.alfgoto.rune.gameplay.quests.QuestObjective;
import com.alfgoto.rune.gameplay.quests.QuestType;
import com.alfgoto.rune.gameplay.quests.QuestTypeRegistry;
import com.alfgoto.rune.gameplay.quests.QuestsModule;
import com.google.auto.service.AutoService;
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

    @Override
    public void enable(RuneContext ctx) {
        QuestTypeRegistry types = ctx.get(QuestTypeRegistry.class);
        ItemRegistry items = ctx.get(ItemRegistry.class);
        PlaceDetector places = ctx.get(PlaceDetector.class);
        types.register(TEST);
        types.register(new QuestType("collect", CoreJobs.MINING, 20, event -> isCollectBlock(event.block())));
        types.register(new QuestType("depth", CoreJobs.MINING, 10, event -> isDeepOre(event.block())));
        types.register(new QuestType("tool", CoreJobs.MINING, 100, event -> isHeldItem(items, event.tool())));
        QuestObjective inPlace = event -> {
            for (Place place : places.placesOf(event.player())) {
                if (place.id().equals(PLACE_ID)) {
                    return true;
                }
            }
            return false;
        };
        types.register(new QuestType("place", CoreJobs.MINING, 50, inPlace));
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
        return items.of(tool).map(RuneItem::id).filter(TOOL_ITEM_ID::equals).isPresent();
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                QuestsModule.class,
                CoreJobsModule.class,
                ItemsModule.class,
                PlacesModule.class,
                CorePlacesModule.class);
    }
}
