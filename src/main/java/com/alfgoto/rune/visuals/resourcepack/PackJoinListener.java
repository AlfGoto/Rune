package com.alfgoto.rune.visuals.resourcepack;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Sends the resource pack, with its hash, to every player who joins. */
public final class PackJoinListener implements Listener {

    /** Stable id, so the client replaces the old Rune pack instead of stacking a second one. */
    static final UUID PACK_ID = UUID.nameUUIDFromBytes("rune-pack".getBytes(StandardCharsets.UTF_8));

    private final PackSettings settings;
    private final String sha1;

    public PackJoinListener(PackSettings settings, String sha1) {
        this.settings = settings;
        this.sha1 = sha1;
    }

    /**
     * The URL a player downloads the pack from. The built-in server uses the address the player
     * joined with, so no public address has to be configured.
     */
    static String url(PackSettings settings, String joinedHost) {
        if (settings.usesExternalUrl()) {
            return settings.externalUrl();
        }
        String host = joinedHost == null || joinedHost.isBlank() ? "localhost" : joinedHost;
        if (host.contains(":")) {
            host = "[" + host + "]"; // IPv6 literal
        }
        return "http://" + host + ":" + settings.port() + PackServer.PATH;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress virtualHost = player.getVirtualHost();
        String host = virtualHost == null ? null : virtualHost.getHostString();
        var info = ResourcePackInfo.resourcePackInfo(PACK_ID, URI.create(url(settings, host)), sha1);
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest().packs(info).build());
    }
}
