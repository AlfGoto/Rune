package com.alfgoto.rune;

import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.LevelSubcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import com.alfgoto.rune.command.XpSubcommand;
import com.alfgoto.rune.content.jobs.CoreJobs;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.storage.PdcPlayerStorage;
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
        getLogger().info("Rune enabled");
    }

    /** The one place where {@code /rune} subcommands are registered. */
    private void registerCommands() {
        Registry<Job> jobs = CoreJobs.registry();
        JobProgress progress = new JobProgress(
                new PdcPlayerStorage(this),
                new LevelCurve(runeConfig.levelCurveBase(), runeConfig.levelCurveExponent()),
                new LevelCurve(runeConfig.accountCurveBase(), runeConfig.accountCurveExponent()));
        List<Subcommand> subcommands = List.of(
                new VersionSubcommand(getPluginMeta().getVersion()),
                new XpSubcommand(jobs, progress),
                new LevelSubcommand(jobs, progress));
        RuneCommand command = new RuneCommand(subcommands);
        var rune = Objects.requireNonNull(getCommand("rune"), "rune command missing from plugin.yml");
        rune.setExecutor(command);
        rune.setTabCompleter(command);
    }
}
