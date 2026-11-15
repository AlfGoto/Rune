package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.gameplay.items.ItemRedrawer;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolStats;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

/**
 * Builds the name and lore of a Rune item from its data: the name in its Rarity colour, then the
 * Rarity line, then its stats. A Rune Tool shows its Harvest Tier, slots, Capacity and Trait; any
 * other item shows a placeholder until it gets stats.
 */
public final class ItemRenderer implements ItemRedrawer {

    private final Function<RuneItem, Optional<ToolStats>> toolStats;

    /** A renderer for items with no tool stats. */
    public ItemRenderer() {
        this(item -> Optional.empty());
    }

    /** {@code toolStats} gives the final stats of a Rune Tool (empty for other items). */
    public ItemRenderer(Function<RuneItem, Optional<ToolStats>> toolStats) {
        this.toolStats = toolStats;
    }

    @Override
    public void redraw(ItemStack stack, RuneItem item) {
        stack.editMeta(meta -> {
            meta.itemName(Text.translatable(item.translationKey(), item.fallbackName())
                    .color(item.rarity().color())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore(item, toolStats.apply(item)));
        });
    }

    static List<Component> lore(RuneItem item, Optional<ToolStats> stats) {
        List<Component> lore = new ArrayList<>();
        lore.add(Text.rarityName(item.rarity()).decoration(TextDecoration.ITALIC, false));
        if (stats.isEmpty()) {
            lore.add(line(Text.translatable("rune.item.lore.stats_placeholder", "No stats yet")
                    .color(NamedTextColor.DARK_GRAY)));
            return lore;
        }
        ToolStats s = stats.get();
        lore.add(stat(
                "rune.item.lore.harvest_tier",
                "Harvest Tier: %s",
                Text.translatable(s.tier().translationKey(), capitalise(s.tier().id()))));
        lore.add(stat("rune.item.lore.slots", "Rune slots: %s", Component.text(s.slots())));
        lore.add(stat("rune.item.lore.capacity", "Capacity: %s", Component.text(s.capacity())));
        s.trait().ifPresent(trait -> lore.add(stat(
                "rune.item.lore.trait",
                "Trait: %s",
                Text.translatable(Text.key("trait", trait), capitalise(trait.replace('_', ' '))))));
        return lore;
    }

    private static Component stat(String key, String fallback, Component value) {
        return line(Text.translatable(key, fallback, value.color(NamedTextColor.WHITE)).color(NamedTextColor.GRAY));
    }

    private static Component line(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

    private static String capitalise(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
