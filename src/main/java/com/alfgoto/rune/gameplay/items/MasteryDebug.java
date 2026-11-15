package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.command.Subcommand;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** {@code /rune debug mastery add <n>}: counts n valuable Job Actions on the Rune Tool you hold. */
public final class MasteryDebug implements Subcommand {

    private final ToolMastery mastery;

    public MasteryDebug(ToolMastery mastery) {
        this.mastery = mastery;
    }

    @Override
    public String name() {
        return "mastery";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() != 2 || !args.get(0).equalsIgnoreCase("add")) {
            error(sender, "rune.mastery.debug.usage", "Usage: /rune debug mastery add <n>");
            return;
        }
        if (!(sender instanceof Player player)) {
            error(sender, "rune.mastery.debug.player_only", "Only a player can do this.");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            amount = 0;
        }
        if (amount < 1) {
            error(sender, "rune.mastery.debug.usage", "Usage: /rune debug mastery add <n>");
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        Optional<MasteryStatus> before = mastery.status(held);
        if (before.isEmpty()) {
            error(sender, "rune.mastery.debug.no_mastery", "Hold a Rune Tool that has Mastery (pickaxe or axe).");
            return;
        }
        if (!mastery.add(held, amount)) {
            error(sender, "rune.mastery.debug.maxed", "This tool already has all its Mastery.");
            return;
        }
        MasteryStatus after = mastery.status(held).orElseThrow();
        sender.sendMessage(Component.translatable(
                "rune.mastery.debug.done",
                "Added %s actions. Mastery: +%s Capacity.",
                Component.text(amount),
                Component.text(after.bonus())));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        return args.size() == 1 && "add".startsWith(args.get(0).toLowerCase()) ? List.of("add") : List.of();
    }

    private static void error(CommandSender sender, String key, String fallback) {
        sender.sendMessage(Component.translatable(key, fallback).color(NamedTextColor.RED));
    }
}
