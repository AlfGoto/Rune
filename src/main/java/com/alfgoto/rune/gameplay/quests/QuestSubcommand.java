package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.text.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune quest list} shows your 3 active Quests. The test tool is {@code /rune debug quest set}. */
public final class QuestSubcommand implements Subcommand {

    private static final String USAGE = "Usage: /rune quest list";

    private final QuestBook book;

    public QuestSubcommand(QuestBook book) {
        this.book = book;
    }

    @Override
    public String name() {
        return "quest";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
            return;
        }
        if (args.size() == 1 && args.get(0).equalsIgnoreCase("list")) {
            list(player);
        } else {
            sender.sendMessage(error("rune.command.quest.usage", USAGE));
        }
    }

    private void list(Player player) {
        player.sendMessage(Component.translatable("rune.command.quest.header", "Active Quests")
                .color(NamedTextColor.GOLD));
        List<Quest> quests = book.active(player.getUniqueId());
        for (int i = 0; i < quests.size(); i++) {
            player.sendMessage(describe("rune.command.quest.entry", "%s. %s - %s (%s): %s", i + 1, quests.get(i)));
        }
        if (quests.isEmpty()) {
            player.sendMessage(error("rune.command.quest.none", "No Quest is available yet."));
        }
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        return args.size() == 1 && "list".startsWith(args.get(0).toLowerCase(java.util.Locale.ROOT))
                ? List.of("list")
                : List.of();
    }

    static Component describe(String key, String fallback, int slot, Quest quest) {
        return Component.translatable(
                key,
                fallback,
                Component.text(slot),
                Component.translatable(quest.type().job().translationKey(), quest.type().job().fallbackName()),
                Component.translatable(quest.type().translationKey(), quest.type().fallbackName()),
                Text.rarityName(quest.tier()),
                Component.text(quest.target()));
    }

    static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
