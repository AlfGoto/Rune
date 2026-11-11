package com.alfgoto.rune;

import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point. */
public class RunePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommands();
        getLogger().info("Rune enabled");
    }

    /** The one place where {@code /rune} subcommands are registered. */
    private void registerCommands() {
        List<Subcommand> subcommands = List.of(new VersionSubcommand(getPluginMeta().getVersion()));
        RuneCommand command = new RuneCommand(subcommands);
        var rune = Objects.requireNonNull(getCommand("rune"), "rune command missing from plugin.yml");
        rune.setExecutor(command);
        rune.setTabCompleter(command);
    }
}
