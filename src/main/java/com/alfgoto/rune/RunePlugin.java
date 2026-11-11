package com.alfgoto.rune;

import com.alfgoto.rune.command.DebugSubcommand;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.visuals.reveal.BlockReveal;
import java.util.List;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point. */
public class RunePlugin extends JavaPlugin implements Listener {

    private RuneConfig runeConfig;
    private BlockReveal blockReveal;

    public RuneConfig runeConfig() {
        return runeConfig;
    }

    public BlockReveal blockReveal() {
        return blockReveal;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runeConfig = RuneConfig.load(getConfig(), getLogger());
        blockReveal = new BlockReveal(this);
        getServer().getPluginManager().registerEvents(this, this);
        registerCommands();
        getLogger().info("Rune enabled");
    }

    @Override
    public void onDisable() {
        if (blockReveal != null) {
            blockReveal.clearAll();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        blockReveal.clear(event.getPlayer().getUniqueId());
    }

    /** The one place where {@code /rune} subcommands are registered. */
    private void registerCommands() {
        List<Subcommand> subcommands = List.of(
                new VersionSubcommand(getPluginMeta().getVersion()), new DebugSubcommand(blockReveal));
        RuneCommand command = new RuneCommand(subcommands);
        var rune = Objects.requireNonNull(getCommand("rune"), "rune command missing from plugin.yml");
        rune.setExecutor(command);
        rune.setTabCompleter(command);
    }
}
