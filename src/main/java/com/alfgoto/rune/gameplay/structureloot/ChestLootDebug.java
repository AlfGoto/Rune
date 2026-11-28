package com.alfgoto.rune.gameplay.structureloot;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /rune debug loot-chest <loot_table>}: rolls the structure loot of one chest loot table for you, as if you
 * had just opened that chest (your real Job Levels apply), prints each result and gives it to you. The table is the
 * full loot table key (for example {@code chests/ancient_city}); {@code minecraft:} is optional.
 */
final class ChestLootDebug implements Subcommand {

    private final StructureLoot structureLoot;
    private final RandomGenerator random;

    ChestLootDebug(StructureLoot structureLoot, RandomGenerator random) {
        this.structureLoot = structureLoot;
        this.random = random;
    }

    @Override
    public String name() {
        return "loot-chest";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() != 1) {
            sender.sendMessage(
                    error("rune.command.debug.loot_chest.usage", "Usage: /rune debug loot-chest <loot_table>"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
            return;
        }
        String table = args.get(0).toLowerCase(Locale.ROOT);
        if (!table.contains(":")) {
            table = "minecraft:" + table;
        }
        if (!structureLoot.tables().contains(table)) {
            sender.sendMessage(error(
                    "rune.command.debug.loot_chest.unknown_table",
                    "No structure loot for: %s",
                    Component.text(table)));
            return;
        }
        List<StructureLoot.Result> results = structureLoot.roll(player.getUniqueId(), table, random);
        sender.sendMessage(Component.translatable(
                        "rune.command.debug.loot_chest.header",
                        "Opened %s: %s structure item(s)",
                        Component.text(table),
                        Component.text(results.size()))
                .color(NamedTextColor.GOLD));
        for (StructureLoot.Result result : results) {
            GiveSubcommand.give(player, result.stack());
            sender.sendMessage(Component.translatable("rune.command.loot.entry", "- %s", result.name()));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        List<String> out = new ArrayList<>();
        if (args.size() == 1) {
            String typed = args.get(0).toLowerCase(Locale.ROOT);
            structureLoot.tables().forEach(table -> {
                if (table.startsWith(typed) || table.substring(table.indexOf(':') + 1).startsWith(typed)) {
                    out.add(table);
                }
            });
        }
        return out;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
