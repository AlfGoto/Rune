package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.trees.TreeScan;
import com.alfgoto.rune.foundations.trees.TreesModule;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.effects.RankValues;
import com.alfgoto.rune.gameplay.items.BrokenToolListener;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.Material;

/** Treefeller: Epic, axe only. The felling is done by {@link TreefellerListener}. */
@AutoService(RuneModule.class)
public final class TreefellerModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                RunesModule.class,
                ItemsModule.class,
                BlockTrackingModule.class,
                EffectsModule.class,
                TriggersModule.class,
                TreesModule.class);
    }

    /**
     * The value is the block cap: base (16) times ratio (2) each Rank, so 16 / 32 / 64, all from config
     * ({@code runes.treefeller}).
     */
    @Override
    public void enable(RuneContext ctx) {
        var config = ctx.config();
        var base = config.runeNumber("treefeller", "base");
        var ratio = config.runeNumber("treefeller", "ratio");
        var maxRank = config.runeNumber("treefeller", "max-rank");
        if (base.isEmpty() || ratio.isEmpty() || maxRank.isEmpty() || ratio.getAsDouble() <= 0) {
            ctx.logger().severe("Config 'runes.treefeller' needs base, ratio (above 0) and max-rank numbers. "
                    + "Rune 'treefeller' is not registered.");
            return;
        }
        Effect<RuneBlockBreakEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(TreefellerListener.EFFECT_ID, EffectTriggers.BLOCK_BREAK)
                        .does(context -> {}));
        ctx.get(RuneRegistry.class)
                .register(new Rune(
                        new RuneItem(TreefellerListener.EFFECT_ID, Rarity.EPIC, Material.PAPER, "treefeller"),
                        effect.with(RankValues.multiplied(
                                base.getAsDouble(), ratio.getAsDouble(), (int) Math.max(1, maxRank.getAsDouble()))),
                        Set.of(Fit.AXE)));
        ctx.listen(new TreefellerListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                ctx.get(BlockTracker.class),
                ctx.get(TreeScan.class),
                new MultiBreak(ctx.get(Triggers.class)),
                ctx.get(BrokenToolListener.class)::isBroken));
    }
}
