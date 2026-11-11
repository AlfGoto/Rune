package com.alfgoto.rune.command;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

/** {@code /rune version}: prints the plugin version. */
public final class VersionSubcommand implements Subcommand {

    private final String version;

    public VersionSubcommand(String version) {
        this.version = version;
    }

    @Override
    public String name() {
        return "version";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        sender.sendMessage(Component.translatable("rune.command.version", "Rune version %s", Component.text(version)));
    }
}
