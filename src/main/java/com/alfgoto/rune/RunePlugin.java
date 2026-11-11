package com.alfgoto.rune;

import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.overrides.LockedGearListener;
import com.alfgoto.rune.overrides.NoEnchantingListener;
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
        registerCommands();
        registerOverrides();
        getLogger().info("Rune enabled");
    }

    /** Vanilla Overrides: enchanting removed, Locked Gear. */
    private void registerOverrides() {
        getServer().getPluginManager().registerEvents(new NoEnchantingListener(), this);
        getServer().getPluginManager().registerEvents(new LockedGearListener(), this);
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
