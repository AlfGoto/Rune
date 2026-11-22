package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.gameplay.items.ItemRedrawer;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.inventory.ItemStack;

/**
 * Tells the resource pack which Rank a Rune stack shows: writes the Rank into {@code custom_model_data}
 * {@code floats[0]}. The Rune's item definition ({@code assets/rune/items/<id>.json}) picks the model with
 * that Rank's numeral (docs/art-direction.md, "Runes"). Runs on every redraw, so a Rank-up updates the look.
 */
public final class RuneModelRenderer implements ItemRedrawer {

    private final RuneRegistry runes;

    public RuneModelRenderer(RuneRegistry runes) {
        this.runes = runes;
    }

    @Override
    public void redraw(ItemStack stack, RuneItem item) {
        if (runes.of(stack).isEmpty()) {
            return;
        }
        stack.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addFloat(runes.rankOf(stack)).build());
    }
}
