package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.config.RhythmConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.effects.Scale;
import com.alfgoto.rune.gameplay.effects.ScaleRegistry;
import com.alfgoto.rune.gameplay.items.ArmorKind;
import com.alfgoto.rune.gameplay.items.ArmorSpec;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.TraitBinding;
import com.alfgoto.rune.gameplay.woodcutting.RhythmRules;
import com.alfgoto.rune.gameplay.woodcutting.RhythmScale;
import com.alfgoto.rune.gameplay.woodcutting.WoodcuttingModule;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Locale;
import java.util.Set;

/**
 * The Common and Uncommon Woodcutting armor with the Rhythm Trait (docs/jobs/woodcutting.md). Each piece's
 * Trait values come from {@code armor.rhythm} in config.yml and follow the {@code rhythm} Scale. Capacity is
 * the Rarity's, with no Trait modifier.
 */
@AutoService(RuneModule.class)
public final class WoodcuttingArmorModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, WoodcuttingModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        Scale rhythm = ctx.get(ScaleRegistry.class).get(RhythmScale.ID);
        RhythmConfig config = ctx.config().rhythm();

        piece(items, config, rhythm, "lumberjacks_cap", Rarity.COMMON, ArmorKind.HELMET);
        piece(items, config, rhythm, "lumberjacks_vest", Rarity.COMMON, ArmorKind.CHESTPLATE);
        piece(items, config, rhythm, "lumberjacks_trousers", Rarity.COMMON, ArmorKind.LEGGINGS);
        piece(items, config, rhythm, "lumberjacks_boots", Rarity.COMMON, ArmorKind.BOOTS);
        piece(items, config, rhythm, "fellers_hood", Rarity.UNCOMMON, ArmorKind.HELMET);
        piece(items, config, rhythm, "fellers_jerkin", Rarity.UNCOMMON, ArmorKind.CHESTPLATE);
        piece(items, config, rhythm, "fellers_leggings", Rarity.UNCOMMON, ArmorKind.LEGGINGS);
        piece(items, config, rhythm, "fellers_boots", Rarity.UNCOMMON, ArmorKind.BOOTS);
    }

    private static void piece(
            ItemRegistry items, RhythmConfig config, Scale rhythm, String id, Rarity rarity, ArmorKind kind) {
        String r = rarity.name().toLowerCase(Locale.ROOT);
        ArmorSpec spec =
                switch (kind) {
                    case HELMET -> ArmorSpec.of(kind)
                            .withTrait(
                                    "rhythm_helmet",
                                    0,
                                    bind(RhythmRules.CHOP_SPEED, config.max(RhythmConfig.CHOP_SPEED, r), rhythm));
                    case CHESTPLATE -> ArmorSpec.of(kind)
                            .withTrait(
                                    "rhythm_chestplate",
                                    0,
                                    bind(
                                            RhythmRules.DAMAGE_REDUCTION,
                                            config.max(RhythmConfig.DAMAGE_REDUCTION, r),
                                            rhythm));
                    case LEGGINGS -> ArmorSpec.of(kind)
                            .withTrait(
                                    "rhythm_leggings",
                                    0,
                                    bind(RhythmRules.MOVE_SPEED, config.max(RhythmConfig.MOVE_SPEED, r), rhythm));
                    case BOOTS -> ArmorSpec.of(kind)
                            .withTrait(
                                    "rhythm_boots",
                                    0,
                                    bind(RhythmRules.JUMP, config.max(RhythmConfig.JUMP, r), rhythm),
                                    bind(
                                            RhythmRules.FALL_REDUCTION,
                                            config.max(RhythmConfig.FALL_REDUCTION, r),
                                            rhythm));
                };
        // The worn layer is one per set: rune:woodcutting_<rarity>.
        items.register(RuneItem.armor(id, rarity, id, spec.withEquipment("rune:woodcutting_" + r)));
    }

    private static TraitBinding bind(String effectId, double value, Scale scale) {
        return new TraitBinding(effectId, value, scale);
    }
}
