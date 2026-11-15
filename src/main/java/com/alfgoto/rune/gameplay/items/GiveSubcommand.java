package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.command.Subcommand;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** {@code /rune give <id> [amount]}: gives yourself a custom item. */
public final class GiveSubcommand implements Subcommand {

    /** A safety limit for one command: a full inventory. */
    static final int MAX_AMOUNT = 36 * 64;

    private final ItemRegistry items;
    private final Map<String, GiveVariant> variants = new LinkedHashMap<>();

    public GiveSubcommand(ItemRegistry items) {
        this.items = items;
    }

    /** Adds {@code /rune give <keyword> ...}, handled by {@code variant} (e.g. {@code give rune <id> [rank]}). */
    public void addVariant(GiveVariant variant) {
        if (variants.putIfAbsent(variant.keyword(), variant) != null) {
            throw new IllegalStateException("Give variant '" + variant.keyword() + "' is registered twice");
        }
    }

    /** Puts {@code stack} in the inventory of {@code player}; what does not fit drops at their feet. */
    public static void give(Player player, ItemStack stack) {
        player.getInventory()
                .addItem(stack)
                .values()
                .forEach(rest -> player.getWorld().dropItemNaturally(player.getLocation(), rest));
    }

    @Override
    public String name() {
        return "give";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
            return;
        }
        if (!args.isEmpty()) {
            GiveVariant variant = variants.get(args.get(0).toLowerCase(Locale.ROOT));
            if (variant != null) {
                variant.execute(player, args.subList(1, args.size()));
                return;
            }
        }
        if (args.isEmpty() || args.size() > 2) {
            sender.sendMessage(error("rune.command.give.usage", "Usage: /rune give <id> [amount]"));
            return;
        }
        RuneItem item = items.find(args.get(0).toLowerCase(Locale.ROOT)).orElse(null);
        if (item == null) {
            sender.sendMessage(error("rune.command.give.unknown", "Unknown item: %s", Component.text(args.get(0))));
            return;
        }
        int amount = 1;
        if (args.size() == 2) {
            try {
                amount = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                amount = 0;
            }
            if (amount < 1 || amount > MAX_AMOUNT) {
                sender.sendMessage(error(
                        "rune.command.give.bad_amount",
                        "The amount must be a number from 1 to %s.",
                        Component.text(MAX_AMOUNT)));
                return;
            }
        }
        int left = amount;
        while (left > 0) {
            ItemStack stack = items.create(item, 1);
            int size = Math.min(left, stack.getMaxStackSize());
            stack.setAmount(size);
            left -= size;
            give(player, stack);
        }
        sender.sendMessage(Component.translatable(
                "rune.command.give.done",
                "Gave %s x%s.",
                Component.translatable(item.translationKey(), item.fallbackName())
                        .color(item.rarity().color()),
                Component.text(amount)));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        if (args.size() > 1) {
            GiveVariant variant = variants.get(args.get(0).toLowerCase(Locale.ROOT));
            return variant == null ? List.of() : variant.complete(args.subList(1, args.size()));
        }
        if (args.size() != 1) {
            return List.of();
        }
        String typed = args.get(0).toLowerCase(Locale.ROOT);
        List<String> ids = new ArrayList<>();
        for (String keyword : variants.keySet()) {
            if (keyword.startsWith(typed)) {
                ids.add(keyword);
            }
        }
        for (RuneItem item : items.all()) {
            if (item.id().startsWith(typed)) {
                ids.add(item.id());
            }
        }
        return ids;
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
