package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.foundations.triggers.Triggers;
import com.alfgoto.rune.foundations.triggers.TriggersModule;
import com.alfgoto.rune.gameplay.blast.BlastCharges;
import com.alfgoto.rune.gameplay.detection.VisibleOre;
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
import com.alfgoto.rune.gameplay.woodcutting.TreeDash;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * The Active pickaxe Runes of docs/jobs/mining.md: Ore Dash, Blast Mine and Great Blast. All numbers come from
 * config {@code runes.<id>}. The blasts themselves are done by {@link BlastListener}.
 */
@AutoService(RuneModule.class)
public final class ActiveMiningRunesModule implements RuneModule {

    private static final String ORE_DASH = "ore_dash";

    private static final Set<Fit> PICKAXE = Set.of(Fit.PICKAXE);

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(RunesModule.class, ItemsModule.class, EffectsModule.class, TriggersModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        BlastCharges charges = new BlastCharges();
        oreDash(ctx);
        blast(ctx, charges, BlastListener.BLAST_MINE, Rarity.EPIC);
        blast(ctx, charges, BlastListener.GREAT_BLAST, Rarity.LEGENDARY);
        ctx.listen(new BlastListener(
                ctx.get(ItemRegistry.class),
                ctx.get(ToolLoadout.class),
                charges,
                new MultiBreak(ctx.get(Triggers.class)),
                ctx.get(BrokenToolListener.class)::isBroken));
    }

    /**
     * Ore Dash: the value is the range in blocks. The click pushes the player to the nearest ore they can see in
     * range. No visible ore: a message and no cooldown.
     */
    private static void oreDash(RuneContext ctx) {
        var config = ctx.config();
        var base = config.runeNumber(ORE_DASH, "base");
        var perRank = config.runeNumber(ORE_DASH, "per-rank");
        var maxRank = config.runeNumber(ORE_DASH, "max-rank");
        var cooldown = config.runeNumber(ORE_DASH, "cooldown");
        var speedPerBlock = config.runeNumber(ORE_DASH, "speed-per-block");
        var maxSpeed = config.runeNumber(ORE_DASH, "max-speed");
        var lift = config.runeNumber(ORE_DASH, "lift");
        var maxChecks = config.runeNumber(ORE_DASH, "max-checks");
        if (base.isEmpty()
                || perRank.isEmpty()
                || maxRank.isEmpty()
                || cooldown.isEmpty()
                || speedPerBlock.isEmpty()
                || maxSpeed.isEmpty()
                || lift.isEmpty()
                || maxChecks.isEmpty()) {
            ctx.logger().severe("Config 'runes.ore_dash' needs base, per-rank, max-rank, cooldown, speed-per-block,"
                    + " max-speed, lift and max-checks numbers. Rune 'ore_dash' is not registered.");
            return;
        }
        // `when` finds the ore, `does` (same click) uses it.
        Map<UUID, Block> found = new HashMap<>();
        Effect<RuneClickEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(ORE_DASH, EffectTriggers.CLICK)
                        .when(context -> {
                            Player player = context.player();
                            found.remove(player.getUniqueId());
                            var ore = VisibleOre.nearest(player, context.value(), (int) maxChecks.getAsDouble());
                            if (ore.isEmpty()) {
                                player.sendActionBar(Component.translatable(
                                                "rune.feedback.ore_dash_none",
                                                "No visible ore within %s blocks",
                                                Component.text((int) context.value()))
                                        .color(NamedTextColor.RED));
                                return false;
                            }
                            found.put(player.getUniqueId(), ore.get());
                            return true;
                        })
                        .does(context -> {
                            Block ore = found.remove(context.player().getUniqueId());
                            if (ore != null) {
                                // The push is the one of Tree Dash: it works on any block.
                                TreeDash.dash(
                                        context.player(),
                                        ore,
                                        speedPerBlock.getAsDouble(),
                                        maxSpeed.getAsDouble(),
                                        lift.getAsDouble());
                            }
                        }));
        ctx.get(RuneRegistry.class)
                .register(Rune.active(
                        new RuneItem(ORE_DASH, Rarity.RARE, Material.PAPER, ORE_DASH),
                        effect.with(
                                base.getAsDouble(),
                                perRank.getAsDouble(),
                                (int) Math.max(1, maxRank.getAsDouble())),
                        PICKAXE,
                        null,
                        RankValues.of(cooldown.getAsDouble(), 0)));
    }

    /**
     * Blast Mine and Great Blast: the click arms the Rune for {@code armed-seconds}; the next pickaxe block mined
     * with the tool sets it off ({@link BlastListener}). The value is the Rank. The cooldown starts at the click and
     * shrinks each Rank: {@code cooldown} + {@code cooldown-per-rank} per Rank above 1.
     */
    private static void blast(RuneContext ctx, BlastCharges charges, String id, Rarity rarity) {
        var config = ctx.config();
        var maxRank = config.runeNumber(id, "max-rank");
        var cooldown = config.runeNumber(id, "cooldown");
        var cooldownPerRank = config.runeNumber(id, "cooldown-per-rank");
        var armedSeconds = config.runeNumber(id, "armed-seconds");
        if (maxRank.isEmpty() || cooldown.isEmpty() || cooldownPerRank.isEmpty() || armedSeconds.isEmpty()) {
            ctx.logger().severe("Config 'runes." + id + "' needs max-rank, cooldown, cooldown-per-rank and"
                    + " armed-seconds numbers. Rune '" + id + "' is not registered.");
            return;
        }
        Effect<RuneClickEvent> effect = ctx.get(EffectRegistry.class)
                .register(Effect.builder(id, EffectTriggers.CLICK)
                        .does(context ->
                                charges.arm(context.player().getUniqueId(), id, armedSeconds.getAsDouble())));
        int max = (int) Math.max(1, maxRank.getAsDouble());
        ctx.get(RuneRegistry.class)
                .register(Rune.active(
                        new RuneItem(id, rarity, Material.PAPER, id),
                        effect.with(1, 1, max),
                        PICKAXE,
                        null,
                        RankValues.of(cooldown.getAsDouble(), cooldownPerRank.getAsDouble(), max)));
    }
}
