package com.alfgoto.rune.visuals.cachereel;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.caches.CacheOpenEvent;
import com.alfgoto.rune.gameplay.caches.CachesModule;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.visuals.menu.MenuModule;
import com.google.auto.service.AutoService;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Shows the Cache reel when a Rune Cache is opened (the loot is given by Gameplay when the reel ends). */
@AutoService(RuneModule.class)
public final class CacheReelModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.listen(new ReelListener(ctx.plugin(), ctx.get(RuneRegistry.class)));
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(CachesModule.class, RunesModule.class, MenuModule.class);
    }

    static final class ReelListener implements Listener {

        private final Plugin plugin;
        private final RuneRegistry runes;

        ReelListener(Plugin plugin, RuneRegistry runes) {
            this.plugin = plugin;
            this.runes = runes;
        }

        @EventHandler
        public void onOpen(CacheOpenEvent event) {
            if (event.revealTicks() <= 0 || event.drops().isEmpty()) {
                return; // given at once: nothing to reveal
            }
            new CacheReel(event, this::filler).start(plugin);
        }

        /** A random Rune shown spinning past (never given). */
        private ItemStack filler() {
            List<Rune> all = runes.all().stream().filter(r -> !r.id().startsWith("test_")).toList();
            if (all.isEmpty()) {
                return new ItemStack(Material.PAPER);
            }
            return runes.create(all.get(ThreadLocalRandom.current().nextInt(all.size())), 1);
        }
    }
}
