package com.alfgoto.rune;

import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.command.VersionSubcommand;
import com.alfgoto.rune.command.WhereSubcommand;
import com.alfgoto.rune.content.places.CorePlaces;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.places.BukkitWorldProbe;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import java.util.List;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/** Plugin entry point. */
public class RunePlugin extends JavaPlugin {

    private RuneConfig runeConfig;
    private PlaceDetector placeDetector;

    public RuneConfig runeConfig() {
        return runeConfig;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        runeConfig = RuneConfig.load(getConfig(), getLogger());
        placeDetector = new PlaceDetector(CorePlaces.registry(), new BukkitWorldProbe());
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                placeDetector.forget(event.getPlayer().getUniqueId());
            }
        }, this);
        registerCommands();
        getLogger().info("Rune enabled");
    }

    /** The one place where {@code /rune} subcommands are registered. */
    private void registerCommands() {
        List<Subcommand> subcommands = List.of(new VersionSubcommand(getPluginMeta().getVersion()), new WhereSubcommand(placeDetector));
        RuneCommand command = new RuneCommand(subcommands);
        var rune = Objects.requireNonNull(getCommand("rune"), "rune command missing from plugin.yml");
        rune.setExecutor(command);
        rune.setTabCompleter(command);
    }
}
