package com.alfgoto.rune.gameplay.caches;

import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.GiveVariant;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** {@code /rune give cache [job] <rarity>}: gives yourself a Rune Cache, for a Job or general. */
final class GiveCacheVariant implements GiveVariant {

    private final Caches caches;
    private final JobRegistry jobs;

    GiveCacheVariant(Caches caches, JobRegistry jobs) {
        this.caches = caches;
        this.jobs = jobs;
    }

    @Override
    public String keyword() {
        return "cache";
    }

    @Override
    public void execute(Player player, List<String> args) {
        if (args.isEmpty() || args.size() > 2) {
            player.sendMessage(error("rune.command.give_cache.usage", "Usage: /rune give cache [job] <rarity>"));
            return;
        }
        String rarityWord = args.get(args.size() - 1);
        Rarity rarity = Rarity.fromId(rarityWord);
        if (rarity == null) {
            player.sendMessage(error(
                    "rune.command.give_cache.unknown_rarity", "Unknown Rarity: %s", Component.text(rarityWord)));
            return;
        }
        String job = args.size() == 2 ? args.get(0).toLowerCase(Locale.ROOT) : null;
        if (job != null && !jobs.registry().contains(job)) {
            player.sendMessage(error("rune.command.give_cache.unknown_job", "Unknown Job: %s", Component.text(job)));
            return;
        }
        GiveSubcommand.give(player, caches.create(rarity, job));
        player.sendMessage(Component.translatable(
                "rune.command.give_cache.done",
                "Gave a %s Cache.",
                Component.translatable(rarity.translationKey(), rarity.englishName()).color(rarity.color())));
    }

    @Override
    public List<String> complete(List<String> args) {
        List<String> out = new ArrayList<>();
        if (args.size() == 1 || args.size() == 2) {
            for (Rarity rarity : Rarity.values()) {
                out.add(rarity.id());
            }
        }
        String typed = args.isEmpty() ? "" : args.get(args.size() - 1).toLowerCase(Locale.ROOT);
        out.removeIf(word -> !word.startsWith(typed));
        return out;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
