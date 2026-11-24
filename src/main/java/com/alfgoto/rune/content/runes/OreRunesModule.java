package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.blocktracking.BlockTrackingModule;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.effects.RankValues;
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
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;

/** The ore Runes that change blocks: Ore Bloom. (Ore Sense is a visual: see visuals.reveal.OreSenseModule.) */
@AutoService(RuneModule.class)
public final class OreRunesModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                RunesModule.class,
                ItemsModule.class,
                BlockTrackingModule.class,
                CoreRunesModule.class,
                EffectsModule.class,
                TriggersModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        registerVeinminer(ctx);
        ctx.listen(new VeinminerListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                ctx.get(BlockTracker.class),
                new MultiBreak(ctx.get(Triggers.class))));
        ctx.listen(new OreBloomListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                ctx.get(BlockTracker.class),
                () -> ThreadLocalRandom.current().nextDouble()));
    }

    /**
     * Veinminer: Legendary, pickaxe only. The value is the block cap: base (8) times ratio (2) each Rank, so 8 / 16
     * / 32, all from config ({@code runes.veinminer}). The mining is done by {@link VeinminerListener}.
     */
    private static void registerVeinminer(RuneContext ctx) {
        var config = ctx.config();
        var base = config.runeNumber("veinminer", "base");
        var ratio = config.runeNumber("veinminer", "ratio");
        var maxRank = config.runeNumber("veinminer", "max-rank");
        if (base.isEmpty() || ratio.isEmpty() || maxRank.isEmpty() || ratio.getAsDouble() <= 0) {
            ctx.logger().severe("Config 'runes.veinminer' needs base, ratio (above 0) and max-rank numbers. "
                    + "Rune 'veinminer' is not registered.");
            return;
        }
        Effect<RuneBlockBreakEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(VeinminerListener.EFFECT_ID, EffectTriggers.BLOCK_BREAK)
                        .does(context -> {}));
        ctx.get(RuneRegistry.class)
                .register(new Rune(
                        new RuneItem(VeinminerListener.EFFECT_ID, Rarity.LEGENDARY, Material.PAPER, "veinminer"),
                        effect.with(RankValues.multiplied(
                                base.getAsDouble(), ratio.getAsDouble(), (int) Math.max(1, maxRank.getAsDouble()))),
                        Set.of(Fit.PICKAXE)));
    }
}
