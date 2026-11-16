package com.alfgoto.rune.gameplay.crafting;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The rank-up rules: 3 identical Runes (same Rune, same Rank) become 1 Rune of the next Rank, for vanilla XP
 * levels. Pure rules, no inventory handling: {@link RankUpListener} puts them on the smithing table.
 */
public final class RankUp {

    /** Why three stacks cannot be ranked up. */
    public enum Refusal {
        /** At least one stack is empty or not a Rune: not a rank-up attempt at all. */
        NOT_RUNES,
        /** The Runes are not the same Rune at the same Rank. */
        DIFFERENT,
        /** The next Rank is not allowed by the Rune's Max Rank. */
        MAX_RANK
    }

    /** The outcome of {@link #plan}: a valid rank-up (refusal is null) or the reason it is refused. */
    public record Plan(@Nullable Rune rune, int fromRank, int cost, @Nullable Refusal refusal) {
        public boolean valid() {
            return refusal == null;
        }
    }

    /** Cost cap, so a huge multiplier cannot overflow. */
    private static final int MAX_COST = 1_000_000;

    private final RuneRegistry runes;
    private final RuneConfig config;

    public RankUp(RuneRegistry runes, RuneConfig config) {
        this.runes = runes;
        this.config = config;
    }

    /** The cost in vanilla XP levels to go up from {@code fromRank}: multiplier to the power of the Rank. */
    public int cost(int fromRank) {
        double cost = Math.pow(config.rankUpCostMultiplier(), fromRank);
        return (int) Math.max(1, Math.min(MAX_COST, Math.round(cost)));
    }

    /** Checks three stacks (the stacks themselves are not changed). */
    public Plan plan(ItemStack first, ItemStack second, ItemStack third) {
        Rune rune = null;
        int rank = 0;
        boolean different = false;
        for (ItemStack stack : new ItemStack[] {first, second, third}) {
            Rune found = stack == null || stack.isEmpty() ? null : runes.of(stack).orElse(null);
            if (found == null) {
                return new Plan(null, 0, 0, Refusal.NOT_RUNES);
            }
            int foundRank = runes.rankOf(stack);
            if (rune == null) {
                rune = found;
                rank = foundRank;
            } else if (rune != found || rank != foundRank) {
                different = true;
            }
        }
        if (different) {
            return new Plan(null, 0, 0, Refusal.DIFFERENT);
        }
        if (!rune.allowsRank(rank + 1)) {
            return new Plan(rune, rank, 0, Refusal.MAX_RANK);
        }
        return new Plan(rune, rank, cost(rank), null);
    }

    /** True when at least one of the stacks is a Rune (even if the others are not). */
    public boolean anyRune(ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty() && runes.of(stack).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /** The clean Rune the player receives. */
    public ItemStack result(Plan plan) {
        return runes.create(plan.rune(), plan.fromRank() + 1);
    }

    /**
     * The result as shown in the output slot: the Rune plus the cost in its lore (the smithing table has
     * nowhere else to show it). A red line tells when the player has too few levels.
     */
    public ItemStack preview(Plan plan, int playerLevels) {
        ItemStack stack = result(plan);
        stack.editMeta(meta -> {
            List<Component> lore = new ArrayList<>(meta.lore() == null ? List.of() : meta.lore());
            lore.add(Component.empty());
            lore.add(Text.translatable("rune.rank_up.cost", "Rank-up cost: %s XP levels", Component.text(plan.cost()))
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false));
            if (playerLevels < plan.cost()) {
                lore.add(Text.translatable(
                                "rune.rank_up.not_enough",
                                "You need %s XP levels.",
                                Component.text(plan.cost()))
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        });
        return stack;
    }
}
