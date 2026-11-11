package com.alfgoto.rune;

import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point. */
public final class RunePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Rune enabled");
    }
}
