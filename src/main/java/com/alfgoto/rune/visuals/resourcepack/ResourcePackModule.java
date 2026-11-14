package com.alfgoto.rune.visuals.resourcepack;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Builds the resource pack zip at startup, serves it and sends it to players on join.
 * With {@code resource-pack.external-url} set, nothing is served: players download from that URL.
 */
@AutoService(RuneModule.class)
public final class ResourcePackModule implements RuneModule {

    private PackServer server;

    @Override
    public void enable(RuneContext ctx) {
        ResourcePackBuilder.Result result;
        try {
            var target = ctx.plugin().getDataFolder().toPath().resolve("rune-pack.zip");
            result = ResourcePackBuilder.build(target);
            ctx.logger().info("Resource pack built: " + result.zip().getFileName() + " (SHA-1 " + result.sha1() + ")");
        } catch (IOException e) {
            ctx.logger().severe("Could not build the resource pack: " + e.getMessage());
            return;
        }

        PackSettings settings = PackSettings.load(ctx.plugin().getConfig(), ctx.logger());
        int servedPort = 0;
        if (settings.usesExternalUrl()) {
            ctx.logger().info("Resource pack is sent from the external URL: " + settings.externalUrl());
        } else {
            try {
                server = PackServer.start(settings.port(), Files.readAllBytes(result.zip()));
                servedPort = server.port();
                ctx.logger().info("Resource pack served on port " + servedPort + " (open it to players)");
            } catch (IOException e) {
                ctx.logger().severe("Could not serve the resource pack on port " + settings.port() + ": "
                        + e.getMessage() + ". Players will not get the pack.");
                return;
            }
        }
        ctx.listen(new PackJoinListener(settings, result.sha1(), servedPort));
    }

    @Override
    public void disable() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
