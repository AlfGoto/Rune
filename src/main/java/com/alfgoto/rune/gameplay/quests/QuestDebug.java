package com.alfgoto.rune.gameplay.quests;

import static com.alfgoto.rune.gameplay.quests.QuestSubcommand.describe;
import static com.alfgoto.rune.gameplay.quests.QuestSubcommand.error;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug quest set <slot> <type> <tier>}: replaces one active Quest, ignoring Job Level gates. */
public final class QuestDebug implements Subcommand {

    private static final String USAGE = "Usage: /rune debug quest set <slot> <type> <tier>";

    private final Registry<QuestType> types;
    private final QuestBook book;

    public QuestDebug(Registry<QuestType> types, QuestBook book) {
        this.types = types;
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
        if (args.size() != 4 || !args.get(0).equalsIgnoreCase("set")) {
            sender.sendMessage(error("rune.debug.quest.usage", USAGE));
            return;
        }
        Integer slot = parseSlot(args.get(1));
        if (slot == null) {
            sender.sendMessage(error(
                    "rune.command.quest.invalid_slot", "Slot must be 1 to %s: %s",
                    Component.text(QuestBook.SLOTS), Component.text(args.get(1))));
            return;
        }
        QuestType type = types.find(args.get(2).toLowerCase(Locale.ROOT)).orElse(null);
        if (type == null) {
            sender.sendMessage(error(
                    "rune.command.quest.unknown_type", "Unknown Quest Type: %s", Component.text(args.get(2))));
            return;
        }
        Rarity tier = Rarity.fromId(args.get(3));
        if (tier == null) {
            sender.sendMessage(error(
                    "rune.command.quest.unknown_tier", "Unknown Quest Tier: %s", Component.text(args.get(3))));
            return;
        }
        Quest quest = book.set(player.getUniqueId(), slot, type, tier);
        sender.sendMessage(describe("rune.command.quest.changed", "Quest %s is now %s - %s (%s): %s", slot, quest));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        List<String> out = new ArrayList<>();
        boolean set = !args.isEmpty() && args.get(0).equalsIgnoreCase("set");
        if (args.size() == 1) {
            addMatching(out, List.of("set"), args.get(0));
        } else if (args.size() == 2 && set) {
            addMatching(out, List.of("1", "2", "3"), args.get(1));
        } else if (args.size() == 3 && set) {
            addMatching(out, types.all().stream().map(QuestType::id).toList(), args.get(2));
        } else if (args.size() == 4 && set) {
            addMatching(out, Arrays.stream(Rarity.values()).map(Rarity::id).toList(), args.get(3));
        }
        return out;
    }

    private static void addMatching(List<String> out, List<String> options, String typed) {
        String lower = typed.toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.startsWith(lower)) {
                out.add(option);
            }
        }
    }

    private static Integer parseSlot(String text) {
        try {
            int slot = Integer.parseInt(text);
            return slot >= 1 && slot <= QuestBook.SLOTS ? slot : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
