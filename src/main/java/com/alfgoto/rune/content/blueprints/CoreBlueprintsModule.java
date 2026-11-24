package com.alfgoto.rune.content.blueprints;

import com.alfgoto.rune.content.items.CoreItemsModule;
import com.alfgoto.rune.content.jobs.CoreJobsModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.blueprints.Blueprint;
import com.alfgoto.rune.gameplay.blueprints.Blueprints;
import com.alfgoto.rune.gameplay.blueprints.BlueprintsModule;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;

/**
 * Registers the Blueprint item and one test Blueprint (4 stone → Test Item). The real Blueprints (Briseroc,
 * Elder Axe, the armor ones) come with the content tickets of their gear.
 */
@AutoService(RuneModule.class)
public final class CoreBlueprintsModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(BlueprintsModule.class, CoreItemsModule.class, CoreJobsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        // No texture is drawn for the Blueprint yet: its item definition (assets/rune/items/blueprint.json)
        // points to the test model. Replace it when the texture exists.
        ctx.get(ItemRegistry.class)
                .register(new RuneItem(Blueprints.ITEM_ID, Rarity.LEGENDARY, Material.NAME_TAG, "blueprint"));
        ctx.get(RecipeBook.class)
                .register(CraftingRecipe.blueprint(
                        "test_blueprint",
                        ctx.get(JobRegistry.class).registry().get("mining"),
                        "test_item",
                        1,
                        List.of("SS", "SS"),
                        Map.of('S', Ingredient.vanilla(Material.STONE))));
        ctx.get(Blueprints.class).register(new Blueprint("test_blueprint"));
    }
}
