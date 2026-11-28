package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectRegistry;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.effects.EffectsModule;
import com.alfgoto.rune.gameplay.effects.RankValues;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.gameplay.woodcutting.Boomerang;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Boomerang (docs/jobs/woodcutting.md), an Active Rune, Epic, axe only. The value is the range in blocks (config
 * {@code runes.boomerang}). The click breaks the first log on the look line as a normal tool break, so Treefeller
 * on the same axe fells the tree; the axe is shown flying out and back. No log in range: nothing happens and no
 * cooldown.
 */
@AutoService(RuneModule.class)
public final class BoomerangModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, EffectsModule.class, TriggersModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        String id = Boomerang.BOOMERANG;
        var config = ctx.config();
        var base = config.runeNumber(id, "base");
        var perRank = config.runeNumber(id, "per-rank");
        var maxRank = config.runeNumber(id, "max-rank");
        var cooldown = config.runeNumber(id, "cooldown");
        var cooldownPerRank = config.runeNumber(id, "cooldown-per-rank");
        if (base.isEmpty()
                || perRank.isEmpty()
                || maxRank.isEmpty()
                || cooldown.isEmpty()
                || cooldownPerRank.isEmpty()) {
            ctx.logger().severe("Config 'runes." + id + "' needs base, per-rank, max-rank, cooldown and"
                    + " cooldown-per-rank numbers. Rune '" + id + "' is not registered.");
            return;
        }
        // `when` finds the log (same click), `does` breaks it.
        Map<UUID, Block> found = new HashMap<>();
        Effect<RuneClickEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(id, EffectTriggers.CLICK)
                        .when(context -> {
                            found.remove(context.player().getUniqueId());
                            var log = Boomerang.firstLog(context.player(), context.value());
                            log.ifPresent(block -> found.put(context.player().getUniqueId(), block));
                            return log.isPresent();
                        })
                        .does(context -> {
                            Block log = found.remove(context.player().getUniqueId());
                            if (log != null) {
                                Boomerang.fly(
                                        ctx.plugin(),
                                        context.player(),
                                        context.event().item(),
                                        log);
                                // A normal tool break: Treefeller on this axe fells the tree.
                                context.player().breakBlock(log);
                            }
                        }));
        // Model: assets/rune/items/boomerang.json, layered with the Rank (docs/art-direction.md, "Runes").
        ctx.get(RuneRegistry.class)
                .register(Rune.active(
                        new RuneItem(id, Rarity.EPIC, Material.PAPER, id),
                        effect.with(
                                base.getAsDouble(),
                                perRank.getAsDouble(),
                                (int) Math.max(1, maxRank.getAsDouble())),
                        Set.of(Fit.AXE),
                        null,
                        RankValues.of(cooldown.getAsDouble(), cooldownPerRank.getAsDouble())));
    }
}
