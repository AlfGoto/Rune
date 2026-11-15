package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.gameplay.items.ItemRedrawer;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

/**
 * Builds the name and lore of a Rune item from its data: the name in its Rarity colour, then the
 * Rarity line, then a placeholder for the stats (filled when items get stats).
 */
public final class ItemRenderer implements ItemRedrawer {

    @Override
    public void redraw(ItemStack stack, RuneItem item) {
        stack.editMeta(meta -> {
            meta.itemName(Text.translatable(item.translationKey(), item.fallbackName())
                    .color(item.rarity().color())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore(item));
        });
    }

    static List<Component> lore(RuneItem item) {
        return List.of(
                Text.rarityName(item.rarity()).decoration(TextDecoration.ITALIC, false),
                Text.translatable("rune.item.lore.stats_placeholder", "No stats yet")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
    }
}
