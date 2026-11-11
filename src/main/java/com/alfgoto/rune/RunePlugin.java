package com.alfgoto.rune;

import com.alfgoto.rune.command.DebugSubcommand;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingListener;
import com.alfgoto.rune.foundations.blocktracking.PdcBlockTracker;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.text.Text;
import java.util.List;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point. */
public class RunePlugin extends JavaPlugin {

    private RuneConfig runeConfig;
    private BlockTracker blockTracker;

    public BlockTracker blockTracker() {
        return blockTracker;
    }

    public RuneConfig runeConfig() {
        return runeConfig;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runeConfig = RuneConfig.load(getConfig(), getLogger());
        blockTracker = new PdcBlockTracker(Text.NAMESPACE);
        getServer().getPluginManager().registerEvents(new BlockTrackingListener(blockTracker), this);
        registerCommands();
        getLogger().info("Rune enabled");
    }

    /** The one place where {@code /rune} subcommands are registered. */
    private void registerCommands() {
        List<Subcommand> subcommands = List.of(new VersionSubcommand(getPluginMeta().getVersion()), new DebugSubcommand(blockTracker));
        RuneCommand command = new RuneCommand(subcommands);
        var rune = Objects.requireNonNull(getCommand("rune"), "rune command missing from plugin.yml");
        rune.setExecutor(command);
        rune.setTabCompleter(command);
    }
}
