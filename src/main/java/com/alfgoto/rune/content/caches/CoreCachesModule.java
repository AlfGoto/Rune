package com.alfgoto.rune.content.caches;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.caches.Caches;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;

/** Registers the Rune Cache items: one per Rarity ({@code rune_cache_<rarity>}). */
@AutoService(RuneModule.class)
public final class CoreCachesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        items().forEach(ctx.get(ItemRegistry.class)::register);
    }

    /** One {@link RuneItem} per Rarity. The model is a placeholder: no Cache texture exists yet. */
    public static List<RuneItem> items() {
        return Arrays.stream(Rarity.values())
                .map(rarity -> new RuneItem(Caches.itemId(rarity), rarity, Material.PAPER, "test_rune"))
                .toList();
    }
}
