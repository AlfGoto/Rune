package com.alfgoto.rune.visuals.caches;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.gameplay.caches.CachesModule;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.visuals.menu.MenuModule;
import com.google.auto.service.AutoService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The Cache reel: plugs the drawn reel menu into {@link Caches}. Closing the menu early (or leaving,
 * or a shutdown) still gives the loot.
 */
@AutoService(RuneModule.class)
public final class CacheReelModule implements RuneModule, Listener {

    private final Set<ReelMenu> active = new HashSet<>();

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(CachesModule.class, RunesModule.class, MenuModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        Caches caches = ctx.get(Caches.class);
        RuneRegistry runes = ctx.get(RuneRegistry.class);
        caches.setReel((player, rarity, drops, finish) -> {
            RandomGenerator random = ThreadLocalRandom.current();
            ReelMenu menu = new ReelMenu(
                    ctx.plugin(), player, rarity, drops, finish, random, filler(runes, caches, random));
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

    /** A random Rune of the Rarity to spin past (never given); a Cache of that Rarity when there is none. */
    static Function<Rarity, ItemStack> filler(RuneRegistry runes, Caches caches, RandomGenerator random) {
        // Built once per opening, not once per spinning item.
        List<Rune> all = runes.all().stream().filter(r -> !r.id().startsWith("test_")).toList();
        return rarity -> {
            List<Rune> matching = all.stream().filter(r -> r.rarity() == rarity).toList();
            if (matching.isEmpty()) {
                return caches.create(rarity, null);
            }
            return runes.create(matching.get(random.nextInt(matching.size())), 1);
        };
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
        if (event.getInventory().getHolder() instanceof ReelMenu menu && !menu.isRetitling()) {
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
