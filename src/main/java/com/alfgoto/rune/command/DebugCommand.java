package com.alfgoto.rune.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/**
 * {@code /rune debug <tool>}. Modules add their own tool with {@code RuneContext.debug(...)}, so no
 * module owns a shared debug class.
 */
public final class DebugCommand implements Subcommand {

    private final Map<String, Subcommand> tools = new TreeMap<>();

    public void register(Subcommand tool) {
        if (tools.putIfAbsent(tool.name(), tool) != null) {
            throw new IllegalArgumentException("/rune debug " + tool.name() + " is registered twice");
        }
    }

    @Override
    public String name() {
        return "debug";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        Subcommand tool = args.isEmpty() ? null : tools.get(args.getFirst().toLowerCase(Locale.ROOT));
        if (tool == null) {
            sender.sendMessage(Component.translatable(
                            "rune.command.debug_usage",
                            "Usage: /rune debug <%s>",
                            Component.text(String.join("|", tools.keySet())))
                    .color(NamedTextColor.RED));
            return;
        }
        tool.execute(sender, args.subList(1, args.size()));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            String typed = args.getFirst().toLowerCase(Locale.ROOT);
            List<String> names = new ArrayList<>();
            for (String name : tools.keySet()) {
                if (name.startsWith(typed)) {
                    names.add(name);
                }
            }
            return names;
        }
        Subcommand tool = args.isEmpty() ? null : tools.get(args.getFirst().toLowerCase(Locale.ROOT));
        return tool == null ? List.of() : tool.complete(sender, args.subList(1, args.size()));
    }
}
