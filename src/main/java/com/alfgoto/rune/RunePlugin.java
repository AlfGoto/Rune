package com.alfgoto.rune;

import com.alfgoto.rune.foundations.config.RuneConfig;
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
        getLogger().info("Rune enabled");
    }
}
