package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Mining speed Runes (Haste, Specialist, Affinity). Content registers, for each speed effect id, the rule
 * that says when it applies (always, on one ore, in one Place). The value per Rank is the effect's value,
 * in percent. Every effect that applies adds up (Stacking).
 */
public final class SpeedBonuses {

    private final ToolLoadout loadout;
    private final Map<String, BiPredicate<Player, Block>> rules = new LinkedHashMap<>();

    public SpeedBonuses(ToolLoadout loadout) {
        this.loadout = loadout;
    }

    /** Declares {@code effectId} as a speed effect that applies when {@code applies} is true. */
    public void register(String effectId, BiPredicate<Player, Block> applies) {
        if (rules.putIfAbsent(effectId, applies) != null) {
            throw new IllegalArgumentException("Speed effect already registered: " + effectId);
        }
    }

    /** The total speed bonus in percent (0 = none) of the Runes installed on {@code tool}, for this block. */
    public double percent(ItemStack tool, Player player, Block block) {
        List<Stacking.Source> sources = loadout.sources(tool, player);
        Set<String> ids = new LinkedHashSet<>();
        for (Stacking.Source source : sources) {
            ids.add(source.effectId());
        }
        double total = 0;
        for (String id : ids) {
            BiPredicate<Player, Block> rule = rules.get(id);
            if (rule != null && rule.test(player, block)) {
                total += Stacking.total(id, sources);
            }
        }
        return total;
    }
}
