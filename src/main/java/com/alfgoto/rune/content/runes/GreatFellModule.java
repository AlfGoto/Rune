package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.trees.TreeScan;
import com.alfgoto.rune.foundations.trees.TreesModule;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
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
import com.alfgoto.rune.gameplay.veins.MultiBreak;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;

/**
 * Great Fell (docs/jobs/woodcutting.md), an Active Rune, Legendary, axe only. The value is the radius in blocks
 * around the player (config {@code runes.great_fell}). The click fells every tree with a natural log in that
 * radius, whole (trunk and canopy), through {@link MultiBreak}: full Job XP and a Cache roll for every block, and
 * tool wear, stopping when the axe is Broken. No tree in range: nothing happens and no cooldown.
 */
@AutoService(RuneModule.class)
public final class GreatFellModule implements RuneModule {

    static final String ID = "great_fell";

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(
                RunesModule.class,
                ItemsModule.class,
                EffectsModule.class,
                TriggersModule.class,
                TreesModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        var config = ctx.config();
        var base = config.runeNumber(ID, "base");
        var perRank = config.runeNumber(ID, "per-rank");
        var maxRank = config.runeNumber(ID, "max-rank");
        var cooldown = config.runeNumber(ID, "cooldown");
        var cooldownPerRank = config.runeNumber(ID, "cooldown-per-rank");
        var treeCap = config.runeNumber(ID, "tree-cap");
        var maxBlocks = config.runeNumber(ID, "max-blocks");
        if (base.isEmpty()
                || perRank.isEmpty()
                || maxRank.isEmpty()
                || cooldown.isEmpty()
                || cooldownPerRank.isEmpty()
                || treeCap.isEmpty()
                || maxBlocks.isEmpty()) {
            ctx.logger().severe("Config 'runes." + ID + "' needs base, per-rank, max-rank, cooldown,"
                    + " cooldown-per-rank, tree-cap and max-blocks numbers. Rune '" + ID + "' is not registered.");
            return;
        }
        ItemRegistry items = ctx.get(ItemRegistry.class);
        TreeScan scan = ctx.get(TreeScan.class);
        MultiBreak multiBreak = new MultiBreak(ctx.get(Triggers.class));
        var isBroken = ctx.get(BrokenToolListener.class);
        // `when` finds the trees (same click), `does` fells them.
        Map<UUID, List<Block>> found = new HashMap<>();
        Effect<RuneClickEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(ID, EffectTriggers.CLICK)
                        .when(context -> {
                            UUID player = context.player().getUniqueId();
                            List<Block> blocks = scan.scanForest(
                                    context.player().getLocation().getBlock(),
                                    (int) context.value(),
                                    (int) treeCap.getAsDouble(),
                                    (int) maxBlocks.getAsDouble());
                            if (blocks.isEmpty()) {
                                found.remove(player);
                                return false;
                            }
                            found.put(player, blocks);
                            return true;
                        })
                        .does(context -> {
                            List<Block> blocks = found.remove(context.player().getUniqueId());
                            if (blocks != null) {
                                context.player()
                                        .getWorld()
                                        .playSound(context.player().getLocation(), Sound.BLOCK_WOOD_BREAK, 2f, 0.6f);
                                multiBreak.breakAll(
                                        context.player(),
                                        blocks,
                                        tool -> items.of(tool).isPresent() && !isBroken.isBroken(tool));
                            }
                        }));
        // Model: assets/rune/items/great_fell.json, layered with the Rank (docs/art-direction.md, "Runes").
        ctx.get(RuneRegistry.class)
                .register(Rune.active(
                        new RuneItem(ID, Rarity.LEGENDARY, Material.PAPER, ID),
                        effect.with(
                                base.getAsDouble(),
                                perRank.getAsDouble(),
                                (int) Math.max(1, maxRank.getAsDouble())),
                        Set.of(Fit.AXE),
                        null,
                        RankValues.of(cooldown.getAsDouble(), cooldownPerRank.getAsDouble())));
    }
}
