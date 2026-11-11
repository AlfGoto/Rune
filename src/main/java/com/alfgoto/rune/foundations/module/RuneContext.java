package com.alfgoto.rune.foundations.module;

import com.alfgoto.rune.command.DebugCommand;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.foundations.config.RuneConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * What a module gets in {@link RuneModule#enable}: the plugin, config, shared services, and ways to
 * register listeners and {@code /rune} subcommands.
 *
 * <p>Services are how modules share objects without going through {@code RunePlugin}: one module
 * calls {@link #provide}, the modules that list it in {@link RuneModule#dependsOn()} call
 * {@link #get}.
 */
public final class RuneContext {

    private final JavaPlugin plugin;
    private final RuneConfig config;
    private final RuneCommand command;
    private final DebugCommand debug;
    private final Map<Class<?>, Object> services = new HashMap<>();

    public RuneContext(JavaPlugin plugin, RuneConfig config, RuneCommand command, DebugCommand debug) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.config = Objects.requireNonNull(config, "config");
        this.command = Objects.requireNonNull(command, "command");
        this.debug = Objects.requireNonNull(debug, "debug");
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public RuneConfig config() {
        return config;
    }

    public Logger logger() {
        return plugin.getLogger();
    }

    /** Shares {@code service} with other modules. One service per type. */
    public <T> T provide(Class<T> type, T service) {
        Objects.requireNonNull(service, "service");
        if (services.putIfAbsent(type, service) != null) {
            throw new IllegalStateException(type.getSimpleName() + " is provided twice");
        }
        return service;
    }

    /** Returns a service another module provided. */
    public <T> T get(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("No " + type.getSimpleName()
                    + " provided. Is the module that provides it listed in dependsOn()?");
        }
        return type.cast(service);
    }

    /** Registers a Bukkit listener for this plugin. */
    public void listen(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    /** Adds {@code /rune <name>}. */
    public void subcommand(Subcommand subcommand) {
        command.register(subcommand);
    }

    /** Adds {@code /rune debug <name>}. Each module brings its own debug tool. */
    public void debug(Subcommand subcommand) {
        debug.register(subcommand);
    }
}
