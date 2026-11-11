package com.alfgoto.rune.visuals.resourcepack;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import java.io.IOException;

/** Generates the resource pack zip in the plugin folder at startup. */
@AutoService(RuneModule.class)
public final class ResourcePackModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        try {
            var target = ctx.plugin().getDataFolder().toPath().resolve("rune-pack.zip");
            var result = ResourcePackBuilder.build(target);
            ctx.logger().info("Resource pack built: " + result.zip().getFileName() + " (SHA-1 " + result.sha1() + ")");
        } catch (IOException e) {
            ctx.logger().severe("Could not build the resource pack: " + e.getMessage());
        }
    }
}
