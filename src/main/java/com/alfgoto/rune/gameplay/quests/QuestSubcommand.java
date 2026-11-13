package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /rune quest list} shows your 3 active Quests.
 * {@code /rune quest set <slot> <type> <tier>} replaces one (tests, ignores Job Level gates).
 */
public final class QuestSubcommand implements Subcommand {

    private static final String USAGE = "Usage: /rune quest list | /rune quest set <slot> <type> <tier>";

    private final Registry<QuestType> types;
    private final QuestBook book;

    public QuestSubcommand(Registry<QuestType> types, QuestBook book) {
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
        if (args.size() == 1 && args.get(0).equalsIgnoreCase("list")) {
            list(player);
        } else if (args.size() == 4 && args.get(0).equalsIgnoreCase("set")) {
            set(player, args);
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

    private void set(Player player, List<String> args) {
        Integer slot = parseSlot(args.get(1));
        if (slot == null) {
            player.sendMessage(error(
                    "rune.command.quest.invalid_slot", "Slot must be 1 to %s: %s",
                    Component.text(QuestBook.SLOTS), Component.text(args.get(1))));
            return;
        }
        QuestType type = types.find(args.get(2).toLowerCase(Locale.ROOT)).orElse(null);
        if (type == null) {
            player.sendMessage(error(
                    "rune.command.quest.unknown_type", "Unknown Quest Type: %s", Component.text(args.get(2))));
            return;
        }
        Rarity tier = tier(args.get(3));
        if (tier == null) {
            player.sendMessage(error(
                    "rune.command.quest.unknown_tier", "Unknown Quest Tier: %s", Component.text(args.get(3))));
            return;
        }
        Quest quest = book.set(player.getUniqueId(), slot, type, tier);
        player.sendMessage(describe("rune.command.quest.changed", "Quest %s is now %s - %s (%s): %s", slot, quest));
    }

    private Component describe(String key, String fallback, int slot, Quest quest) {
        return Component.translatable(
                key,
                fallback,
                Component.text(slot),
                Component.translatable(quest.type().job().translationKey(), quest.type().job().fallbackName()),
                Component.translatable(quest.type().translationKey(), quest.type().fallbackName()),
                Text.rarityName(quest.tier()),
                Component.text(quest.target()));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        List<String> out = new ArrayList<>();
        if (args.size() == 1) {
            addMatching(out, List.of("list", "set"), args.get(0));
        } else if (args.size() == 2 && args.get(0).equalsIgnoreCase("set")) {
            addMatching(out, List.of("1", "2", "3"), args.get(1));
        } else if (args.size() == 3 && args.get(0).equalsIgnoreCase("set")) {
            addMatching(out, types.all().stream().map(QuestType::id).toList(), args.get(2));
        } else if (args.size() == 4 && args.get(0).equalsIgnoreCase("set")) {
            addMatching(out, java.util.Arrays.stream(Rarity.values()).map(Rarity::id).toList(), args.get(3));
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

    private static Rarity tier(String text) {
        for (Rarity rarity : Rarity.values()) {
            if (rarity.id().equalsIgnoreCase(text)) {
                return rarity;
            }
        }
        return null;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
