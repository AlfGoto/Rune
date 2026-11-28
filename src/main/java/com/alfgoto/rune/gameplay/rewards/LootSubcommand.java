package com.alfgoto.rune.gameplay.rewards;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /rune loot roll <table> [rarity] [job]}: rolls a loot table as a source of that Rarity
 * (default Common, no Job), prints each result and gives them to you.
 */
public final class LootSubcommand implements Subcommand {

    private final Registry<LootTable> tables;
    private final LootRoller roller;
    private final RandomGenerator random;

    public LootSubcommand(Registry<LootTable> tables, LootRoller roller, RandomGenerator random) {
        this.tables = tables;
        this.roller = roller;
        this.random = random;
    }

    @Override
    public String name() {
        return "loot";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 2 || args.size() > 4 || !args.get(0).equalsIgnoreCase("roll")) {
            sender.sendMessage(error("rune.command.loot.usage", "Usage: /rune loot roll <table> [rarity] [job]"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
            return;
        }
        LootTable table = tables.find(args.get(1).toLowerCase(Locale.ROOT)).orElse(null);
        if (table == null) {
            sender.sendMessage(error(
                    "rune.command.loot.unknown_table", "Unknown loot table: %s", Component.text(args.get(1))));
            return;
        }
        Rarity rarity = Rarity.COMMON;
        if (args.size() >= 3) {
            rarity = Rarity.fromId(args.get(2));
            if (rarity == null) {
                sender.sendMessage(error(
                        "rune.command.loot.unknown_rarity", "Unknown Rarity: %s", Component.text(args.get(2))));
                return;
            }
        }
        String job = args.size() == 4 ? args.get(3).toLowerCase(Locale.ROOT) : null;
        List<Drop> drops = roller.roll(table, new RollContext(player.getUniqueId(), rarity, job), random);
        sender.sendMessage(Component.translatable(
                        "rune.command.loot.header",
                        "Rolled %s as %s: %s result(s)",
                        Component.text(table.id()),
                        Text.rarityName(rarity),
                        Component.text(drops.size()))
                .color(NamedTextColor.GOLD));
        for (Drop drop : drops) {
            drop.give(player);
            sender.sendMessage(Component.translatable("rune.command.loot.entry", "- %s", drop.describe()));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        List<String> out = new ArrayList<>();
        String typed = args.isEmpty() ? "" : args.get(args.size() - 1).toLowerCase(Locale.ROOT);
        if (args.size() == 1) {
            out.add("roll");
        } else if (args.size() == 2) {
            tables.all().forEach(table -> out.add(table.id()));
        } else if (args.size() == 3) {
            for (Rarity rarity : Rarity.values()) {
                out.add(rarity.id());
            }
        }
        out.removeIf(word -> !word.startsWith(typed));
        return out;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
