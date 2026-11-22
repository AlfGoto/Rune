package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.google.auto.service.AutoService;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.bukkit.inventory.ItemStack;

/**
 * Woodcutting Runes that act on leaves and movement (Leaf Fall, Orchard, Canopy Stride) and the Trait extras of
 * the Birch, Spruce, Poplar and Jungle axes.
 */
@AutoService(RuneModule.class)
public final class WoodcuttingModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, RunesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        ToolLoadout loadout = ctx.get(ToolLoadout.class);
        ctx.listen(new LeafListener(items, loadout, () -> ThreadLocalRandom.current().nextDouble()));
        ctx.listen(new CanopyListener(ctx.plugin(), items, loadout));
        // Trait extras of the Birch, Spruce, Poplar and Jungle axes.
        int maxLeaves = (int) ctx.config().traitNumber(AxeTraits.FELLED_TREE_LEAF_FALL, "max_leaves", 600);
        int leafReach = (int) ctx.config().traitNumber(AxeTraits.FELLED_TREE_LEAF_FALL, "reach", 7);
        ctx.listen(new FelledTreeTraitListener(ctx.plugin(), items, loadout, maxLeaves, leafReach));
        Predicate<ItemStack> isBroken = ctx.get(BrokenToolListener.class)::isBroken;
        PowderSnowListener powderSnow = new PowderSnowListener(ctx.plugin(), items, loadout, isBroken);
        ctx.listen(powderSnow);
        powderSnow.start();
        ctx.listen(new ClimbListener(items, loadout, isBroken));
    }
}
