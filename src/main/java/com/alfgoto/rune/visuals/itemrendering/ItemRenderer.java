package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.gameplay.items.ItemRedrawer;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

/**
 * Builds the name and lore of a Rune item from its data: the name in its Rarity colour, then the
 * Rarity line, then the stats. A Rune shows its Rank, Capacity Cost and where it fits; other items
 * show a placeholder until they get stats.
 */
public final class ItemRenderer implements ItemRedrawer {

    private final RuneRegistry runes;

    public ItemRenderer(RuneRegistry runes) {
        this.runes = runes;
    }

    @Override
    public void redraw(ItemStack stack, RuneItem item) {
        stack.editMeta(meta -> {
            meta.itemName(Text.translatable(item.translationKey(), item.fallbackName())
                    .color(item.rarity().color())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(runes.find(item.id())
                    .map(rune -> runeLore(rune, runes.rankOf(stack)))
                    .orElseGet(() -> lore(item)));
        });
    }

    static List<Component> lore(RuneItem item) {
        return List.of(
                Text.rarityName(item.rarity()).decoration(TextDecoration.ITALIC, false),
                Text.translatable("rune.item.lore.stats_placeholder", "No stats yet")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
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
