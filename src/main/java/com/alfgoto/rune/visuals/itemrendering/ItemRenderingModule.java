package com.alfgoto.rune.visuals.itemrendering;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.ToolMastery;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** Item Rendering: hooks the renderer into the ItemRegistry so items draw themselves. */
@AutoService(RuneModule.class)
public final class ItemRenderingModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        items.addRedrawer(new ItemRenderer(
                        ctx.get(RuneRegistry.class), items::toolStats, ctx.get(ToolMastery.class)::status)
                .withArmor(items::armorStats));
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class);
    }
}
