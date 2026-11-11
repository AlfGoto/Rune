package com.alfgoto.rune.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/** Root {@code /rune} command. Routes to subcommands and completes their names. */
public final class RuneCommand implements CommandExecutor, TabCompleter {

    public static final String PERMISSION = "rune.admin";

    private final Map<String, Subcommand> subcommands = new TreeMap<>();

    public RuneCommand(List<Subcommand> subcommands) {
        for (Subcommand subcommand : subcommands) {
            this.subcommands.put(subcommand.name(), subcommand);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(Component.translatable(
                            "rune.command.no_permission", "You do not have permission to do this.")
                    .color(NamedTextColor.RED));
            return true;
        }
        Subcommand subcommand = args.length == 0 ? null : subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand == null) {
            sender.sendMessage(Component.translatable(
                            "rune.command.usage",
                            "Usage: /rune <%s>",
                            Component.text(String.join("|", subcommands.keySet())))
                    .color(NamedTextColor.RED));
            return true;
        }
        subcommand.execute(sender, rest(args));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION) || args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            String typed = args[0].toLowerCase(Locale.ROOT);
            List<String> names = new ArrayList<>();
            for (String name : subcommands.keySet()) {
                if (name.startsWith(typed)) {
                    names.add(name);
                }
            }
            return names;
        }
        Subcommand subcommand = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        return subcommand == null ? List.of() : subcommand.complete(sender, rest(args));
    }

    private static List<String> rest(String[] args) {
        return List.of(args).subList(1, args.length);
    }
}
