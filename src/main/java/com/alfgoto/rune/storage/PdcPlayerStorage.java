package com.alfgoto.rune.storage;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * {@link PlayerStorage} backed by the player's PersistentDataContainer. Data is saved with the
 * player file, so it survives relog and server restart.
 *
 * <p>Paper cannot read the container of an offline player: every call needs the player online.
 *
 * <p>The PDC is not thread-safe: every call must run on the main server thread, never async.
 */
public final class PdcPlayerStorage implements PlayerStorage {

    private final Plugin plugin;

    public PdcPlayerStorage(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<String> get(UUID player, String key) {
        return Optional.ofNullable(
                container(player).get(namespaced(key), PersistentDataType.STRING));
    }

    @Override
    public void set(UUID player, String key, String value) {
        container(player).set(namespaced(key), PersistentDataType.STRING, value);
    }

    @Override
    public void remove(UUID player, String key) {
        container(player).remove(namespaced(key));
    }

    private NamespacedKey namespaced(String key) {
        return new NamespacedKey(plugin, key);
    }

    private static PersistentDataContainer container(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            throw new IllegalStateException(
                    "Player " + uuid + " is offline: PDC storage needs an online player");
        }
        return player.getPersistentDataContainer();
    }
}
