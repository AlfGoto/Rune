package com.alfgoto.rune.visuals.caches;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.gameplay.caches.CachesModule;
import com.google.auto.service.AutoService;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * The Cache reel: plugs a spinning reel menu into {@link Caches}. Closing the menu early (or leaving,
 * or a shutdown) still gives the loot.
 */
@AutoService(RuneModule.class)
public final class CacheReelModule implements RuneModule, Listener {

    private final Set<ReelMenu> active = new HashSet<>();

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(CachesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ctx.get(Caches.class).setReel((player, rarity, drops, finish) -> {
            ReelMenu menu = new ReelMenu(ctx.plugin(), player, drops, finish, ThreadLocalRandom.current());
            active.add(menu);
            try {
                menu.start();
                if (menu.isDone()) {
                    active.remove(menu); // the menu did not open: the loot was already given
                }
            } catch (RuntimeException e) {
                active.remove(menu);
                throw e;
            }
        });
        ctx.listen(this);
    }

    @Override
    public void disable() {
        for (ReelMenu menu : Set.copyOf(active)) {
            menu.complete();
        }
        active.clear();
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof ReelMenu menu) {
            menu.complete();
            active.remove(menu);
        }
    }

    /** Explicit quit path: do not rely on the server firing a close event on disconnect. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (ReelMenu menu : Set.copyOf(active)) {
            if (menu.isFor(event.getPlayer())) {
                menu.complete();
                active.remove(menu);
            }
        }
    }
}
