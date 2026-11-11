package com.alfgoto.rune.command;

import com.alfgoto.rune.foundations.triggers.EventDebug;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/** {@code /rune debug events}: toggles the console log of Rune events. */
public final class DebugSubcommand implements Subcommand {

    private static final String EVENTS = "events";

    private final EventDebug eventDebug;

    public DebugSubcommand(EventDebug eventDebug) {
        this.eventDebug = eventDebug;
    }

    @Override
    public String name() {
        return "debug";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() != 1 || !args.get(0).equalsIgnoreCase(EVENTS)) {
            sender.sendMessage(Component.translatable("rune.command.debug.usage", "Usage: /rune debug <events>")
                    .color(NamedTextColor.RED));
            return;
        }
        boolean on = eventDebug.toggle();
        sender.sendMessage(
                on
                        ? Component.translatable("rune.command.debug.events.on", "Event debug log: on")
                        : Component.translatable("rune.command.debug.events.off", "Event debug log: off"));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        return args.size() == 1 && EVENTS.startsWith(args.get(0).toLowerCase(Locale.ROOT))
                ? List.of(EVENTS)
                : List.of();
    }
}
