package com.alfgoto.rune.foundations.triggers;

import com.alfgoto.rune.command.Subcommand;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/** {@code /rune debug events}: toggles the console log of Rune events. */
public final class EventsDebug implements Subcommand {

    private final EventDebug eventDebug;

    public EventsDebug(EventDebug eventDebug) {
        this.eventDebug = eventDebug;
    }

    @Override
    public String name() {
        return "events";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        boolean on = eventDebug.toggle();
        sender.sendMessage(
                on
                        ? Component.translatable("rune.debug.events.on", "Event debug log: on")
                        : Component.translatable("rune.debug.events.off", "Event debug log: off"));
    }
}
