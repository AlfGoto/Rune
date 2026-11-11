package com.alfgoto.rune;

import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.visuals.resourcepack.ResourcePackBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point. */
public class RunePlugin extends JavaPlugin {

    private RuneConfig runeConfig;

    public RuneConfig runeConfig() {
        return runeConfig;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runeConfig = RuneConfig.load(getConfig(), getLogger());
        buildResourcePack();
        registerCommands();
        getLogger().info("Rune enabled");
    }

    /** Generates the resource pack zip in the plugin folder at startup. */
    private void buildResourcePack() {
        try {
            var result = ResourcePackBuilder.build(getDataFolder().toPath().resolve("rune-pack.zip"));
            getLogger().info("Resource pack built: " + result.zip().getFileName() + " (SHA-1 " + result.sha1() + ")");
        } catch (IOException e) {
            getLogger().severe("Could not build the resource pack: " + e.getMessage());
        }
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
