package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.gameplay.items.ItemRedrawer;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolStats;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
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
 * Rarity line, then its stats. A Rune shows its Rank, Capacity Cost and where it fits; a Rune Tool
 * shows its Harvest Tier, slots, Capacity and Trait; any other item shows a placeholder until it
 * gets stats.
 */
public final class ItemRenderer implements ItemRedrawer {

    private final RuneRegistry runes;
    private final Function<RuneItem, Optional<ToolStats>> toolStats;

    /** {@code toolStats} gives the final stats of a Rune Tool (empty for other items). */
    public ItemRenderer(RuneRegistry runes, Function<RuneItem, Optional<ToolStats>> toolStats) {
        this.runes = runes;
        this.toolStats = toolStats;
    }

    @Override
    public void redraw(ItemStack stack, RuneItem item) {
        stack.editMeta(meta -> {
            meta.itemName(Text.translatable(item.translationKey(), item.fallbackName())
                    .color(item.rarity().color())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(runes.find(item.id())
                    .map(rune -> runeLore(rune, runes.rankOf(stack)))
                    .orElseGet(() -> lore(item, toolStats.apply(item))));
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

    private List<Component> runeLore(Rune rune, int rank) {
        Component rankText = rune.maxRank().isPresent()
                ? Text.translatable(
                        "rune.item.lore.rank_max",
                        "Rank: %s / %s",
                        Component.text(rank),
                        Component.text(rune.maxRank().getAsInt()))
                : Text.translatable("rune.item.lore.rank", "Rank: %s", Component.text(rank));
        List<Component> fits = new ArrayList<>();
        for (Fit fit : rune.fits()) {
            fits.add(Text.translatable(fit.translationKey(), fit.englishName()));
        }
        return List.of(
                Text.rarityName(rune.rarity()).decoration(TextDecoration.ITALIC, false),
                gray(rankText),
                gray(Text.translatable(
                        "rune.item.lore.capacity_cost",
                        "Capacity Cost: %s",
                        Component.text(runes.capacityCost(rune, rank)))),
                gray(Text.translatable(
                        "rune.item.lore.fits",
                        "Fits: %s",
                        Component.join(net.kyori.adventure.text.JoinConfiguration.commas(true), fits))));
    }

    private static Component gray(Component component) {
        return component.color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false);
    }
}
