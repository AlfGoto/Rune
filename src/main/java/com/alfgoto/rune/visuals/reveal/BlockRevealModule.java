package com.alfgoto.rune.visuals.reveal;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.google.auto.service.AutoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Wires Block Reveal: shares it as a service, clears it on quit and on disable, adds {@code /rune debug reveal}. */
@AutoService(RuneModule.class)
public final class BlockRevealModule implements RuneModule {

    private BlockReveal reveal;

    @Override
    public void enable(RuneContext ctx) {
        reveal = ctx.provide(BlockReveal.class, new BlockReveal(ctx.plugin()));
        ctx.listen(new QuitListener(reveal));
        ctx.debug(new RevealDebug(reveal));
    }

    @Override
    public void disable() {
        if (reveal != null) {
            reveal.clearAll();
        }
    }

    private record QuitListener(BlockReveal reveal) implements Listener {
        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            reveal.clear(event.getPlayer().getUniqueId());
        }
    }
}
