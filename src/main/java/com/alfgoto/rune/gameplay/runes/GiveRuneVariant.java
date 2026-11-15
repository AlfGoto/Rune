package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.GiveVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** {@code /rune give rune <id> [rank]}: gives yourself one Rune at a Rank (default 1). */
final class GiveRuneVariant implements GiveVariant {

    private final RuneRegistry runes;

    GiveRuneVariant(RuneRegistry runes) {
        this.runes = runes;
    }

    @Override
    public String keyword() {
        return "rune";
    }

    @Override
    public void execute(Player player, List<String> args) {
        if (args.isEmpty() || args.size() > 2) {
            player.sendMessage(error("rune.command.give_rune.usage", "Usage: /rune give rune <id> [rank]"));
            return;
        }
        Rune rune = runes.find(args.get(0).toLowerCase(Locale.ROOT)).orElse(null);
        if (rune == null) {
            player.sendMessage(error("rune.command.give_rune.unknown", "Unknown Rune: %s", Component.text(args.get(0))));
            return;
        }
        int rank = 1;
        if (args.size() == 2) {
            try {
                rank = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                rank = 0;
            }
        }
        if (!rune.allowsRank(rank)) {
            String max = rune.maxRank().isPresent() ? "1 to " + rune.maxRank().getAsInt() : "1 or more";
            player.sendMessage(error(
                    "rune.command.give_rune.bad_rank",
                    "The Rank of this Rune must be a number from %s.",
                    Component.text(max)));
            return;
        }
        GiveSubcommand.give(player, runes.create(rune, rank));
        player.sendMessage(Component.translatable(
                "rune.command.give_rune.done",
                "Gave %s, Rank %s.",
                Component.translatable(rune.item().translationKey(), rune.item().fallbackName())
                        .color(rune.rarity().color()),
                Component.text(rank)));
    }

    @Override
    public List<String> complete(List<String> args) {
        if (args.size() != 1) {
            return List.of();
        }
        String typed = args.get(0).toLowerCase(Locale.ROOT);
        List<String> ids = new ArrayList<>();
        for (Rune rune : runes.all()) {
            if (rune.id().startsWith(typed)) {
                ids.add(rune.id());
            }
        }
        return ids;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
