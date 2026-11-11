package com.alfgoto.rune.command;

import java.util.List;
import org.bukkit.command.CommandSender;

/** One subcommand of {@code /rune}. */
public interface Subcommand {

    /** The word typed after {@code /rune}. Lower case. */
    String name();

    void execute(CommandSender sender, List<String> args);

    /** Tab completions for the arguments after the subcommand name. */
    default List<String> complete(CommandSender sender, List<String> args) {
        return List.of();
    }
}
