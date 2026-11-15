package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Item Rendering: hooks the renderer into the ItemRegistry so items draw themselves. */
@AutoService(RuneModule.class)
public final class ItemRenderingModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ctx.get(ItemRegistry.class).addRedrawer(new ItemRenderer());
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class);
    }
}
