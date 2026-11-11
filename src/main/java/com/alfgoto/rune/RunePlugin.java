package com.alfgoto.rune;

import com.alfgoto.rune.command.DebugCommand;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.module.ModuleLoader;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.storage.PdcPlayerStorage;
import com.alfgoto.rune.storage.PlayerStorage;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin entry point. Do not add features here: write a {@link RuneModule} instead (ADR 0004).
 * This class only sets up the shared Foundations and enables every module.
 */
public class RunePlugin extends JavaPlugin {

    private RuneContext context;
    private List<RuneModule> modules = List.of();

    public RuneContext context() {
        return context;
    }

    public RuneConfig runeConfig() {
        return context.config();
    }

    /** Enabled modules, in enable order. */
    public List<RuneModule> modules() {
        return modules;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RuneConfig config = RuneConfig.load(getConfig(), getLogger());
        RuneCommand command = new RuneCommand(List.of());
        DebugCommand debug = new DebugCommand();
        command.register(debug);
        context = new RuneContext(this, config, command, debug);
        context.provide(PlayerStorage.class, new PdcPlayerStorage(this));

        modules = ModuleLoader.load(getClassLoader());
        for (RuneModule module : modules) {
            module.enable(context);
        }

        var rune = Objects.requireNonNull(getCommand("rune"), "rune command missing from plugin.yml");
        rune.setExecutor(command);
        rune.setTabCompleter(command);
        getLogger().info("Rune enabled (" + modules.size() + " modules)");
    }

    @Override
    public void onDisable() {
        for (RuneModule module : modules.reversed()) {
            try {
                module.disable();
            } catch (RuntimeException e) {
                getLogger().log(Level.SEVERE, "Could not disable " + module.getClass().getSimpleName(), e);
            }
        }
    }
}
