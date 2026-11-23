package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.gameplay.runes.HeldTraits;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.ArmorKind;
import com.alfgoto.rune.gameplay.items.ArmorSpec;
import com.alfgoto.rune.gameplay.items.Codex;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.items.TraitBinding;
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.Material;

/** Registers the core items into the ItemRegistry: test items (with the test axe), the Components and the first Mining armor. */
@AutoService(RuneModule.class)
public final class CoreItemsModule implements RuneModule {

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class);
    }

    @Override
    public void enable(RuneContext ctx) {
        ItemRegistry items = ctx.get(ItemRegistry.class);
        // Uses the test model of the resource pack (assets/rune/items/test_rune.json).
        items.register(new RuneItem("test_item", Rarity.COMMON, Material.PAPER, "test_rune"));
        // Common = stone Harvest Tier: it mines iron ore but gives no drops on diamond ore.
        // Shows the Flint Pickaxe texture so the pack can be checked in game.
        items.register(RuneItem.tool("test_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "flint_pickaxe", ToolSpec.DEFAULT));
        // Common axe, same as the test pickaxe. Shows the Flint Hatchet texture.
        items.register(RuneItem.tool("test_axe", Rarity.COMMON, ToolKind.AXE, "flint_hatchet", ToolSpec.DEFAULT));
        // Test content for Traits (#178): smelt 100 + haste -20 (slower), Capacity -1.
        items.register(RuneItem.tool(
                "test_trait_pickaxe",
                Rarity.COMMON,
                ToolKind.PICKAXE,
                "flint_pickaxe",
                ToolSpec.DEFAULT.withTrait(
                        "test_trait", -1, TraitBinding.always("smelt", 100), TraitBinding.always("haste", -20))));
        // Common helmet: leather protection, 1 slot, Capacity 3. Test item: keeps the test model.
        items.register(RuneItem.armor("test_helmet", Rarity.COMMON, "test_rune", ArmorSpec.of(ArmorKind.HELMET)));
        miningArmor(items);
        // The Rune Codex (soulbound, opens the menus). Model: assets/rune/items/rune_codex.json.
        items.register(new RuneItem(Codex.ID, Rarity.COMMON, Material.BOOK, Codex.ID));
        // Mining pickaxes (docs/jobs/mining.md). Common = stone tier, Capacity 3. The Trait is an id plus a
        // Capacity modifier for now: the "mines slower / faster" and durability numbers are not decided, so
        // speed and durability stay at the stone-tier default. Coal's Trait has Capacity -1 (3 - 1 = 2).
        items.register(RuneItem.tool("flint_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "flint_pickaxe",
                ToolSpec.DEFAULT.withTrait("flint_pickaxe", 0)));
        items.register(RuneItem.tool("coal_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "coal_pickaxe",
                ToolSpec.DEFAULT.withTrait("coal_pickaxe", -1)));
        biomePickaxes(items, ctx.config().tools());
        // Detection pickaxes. Bone: Uncommon, iron tier, Capacity 6. Echo: Rare, iron tier (-1), Capacity 10 +2.
        // Numbers are in config: runes.ore_sense.base (Bone's chance), runes.speed.affinity.per-rank (Deep Dark).
        double oreSense = ctx.config().runeNumber("ore_sense", "base").orElse(5);
        items.register(RuneItem.tool("bone_pickaxe", Rarity.UNCOMMON, ToolKind.PICKAXE, "bone_pickaxe",
                ToolSpec.DEFAULT.withTrait("bone_pickaxe", 0, TraitBinding.always("ore_sense", oreSense))));
        // The Echo's Capacity modifier is runes.echo_ping.capacity-modifier (default 2).
        int echoCapacity = (int) ctx.config().runeNumber("echo_ping", "capacity-modifier").orElse(2);
        items.register(RuneItem.tool("echo_pickaxe", Rarity.RARE, ToolKind.PICKAXE, "echo_pickaxe",
                ToolSpec.DEFAULT.withTierOffset(-1).withTrait("echo_pickaxe", echoCapacity,
                        TraitBinding.always("affinity_deep_dark", ctx.config().speedRunes().affinityPerRank()),
                        TraitBinding.always("echo_ping", 1) /* on/off flag: any value above 0 */)));
        // The 7 Components (docs/jobs/mining.md). Rarity follows what each one is used by.
        // Paper base: vanilla Map/Book recipes would accept it as paper (open, see design-decisions.md).
        component(items, "reinforced_handle", Rarity.UNCOMMON);
        component(items, "reinforced_lining", Rarity.UNCOMMON);
        component(items, "gem_setting", Rarity.RARE);
        component(items, "echo_lens", Rarity.RARE);
        component(items, "deep_core", Rarity.EPIC);
        component(items, "abyssal_plate", Rarity.EPIC);
        component(items, "legendary_core", Rarity.LEGENDARY);
    }

    /**
     * The biome pickaxes (docs/jobs/mining.md): an Affinity Trait in their Place plus one extra. The numbers
     * come from {@code tools.traits} in the config. Lichen is Uncommon at stone tier (-1) with Capacity +1.
     */
    private static void biomePickaxes(ItemRegistry items, ToolConfig tools) {
        items.register(RuneItem.tool("lichen_pickaxe", Rarity.UNCOMMON, ToolKind.PICKAXE, "lichen_pickaxe",
                ToolSpec.DEFAULT.withTierOffset(-1).withTrait("lichen_pickaxe", 1,
                        TraitBinding.always(BiomePickaxeTraits.AFFINITY_LUSH_CAVES, tools.traitAffinity()),
                        TraitBinding.always(HeldTraits.GLOW, tools.traitGlowLight()))));
        items.register(RuneItem.tool("fiery_pickaxe", Rarity.RARE, ToolKind.PICKAXE, "fiery_pickaxe",
                ToolSpec.DEFAULT.withTrait("fiery_pickaxe", 0,
                        TraitBinding.always(BiomePickaxeTraits.AFFINITY_NETHER, tools.traitAffinity()),
                        TraitBinding.always("smelt", tools.traitFierySmelt()))));
        items.register(RuneItem.tool("emerald_pickaxe", Rarity.RARE, ToolKind.PICKAXE, "emerald_pickaxe",
                ToolSpec.DEFAULT.withTrait("emerald_pickaxe", 0,
                        TraitBinding.always(BiomePickaxeTraits.AFFINITY_MOUNTAINS, tools.traitAffinity()),
                        TraitBinding.always(BiomePickaxeTraits.MOUNTAIN_MOVE_SPEED, tools.traitMountainMoveSpeed()))));
        items.register(RuneItem.tool("prismarine_pickaxe", Rarity.RARE, ToolKind.PICKAXE, "prismarine_pickaxe",
                ToolSpec.DEFAULT.withTrait("prismarine_pickaxe", 0,
                        TraitBinding.always(BiomePickaxeTraits.AFFINITY_OCEAN, tools.traitAffinity()),
                        TraitBinding.always(HeldTraits.NO_WATER_PENALTY, 1),
                        TraitBinding.always(HeldTraits.NO_AIR_PENALTY, 1))));
    }

    /**
     * The Common and Uncommon Mining armor (docs/jobs/mining.md). Stats follow the Rarity. The Trait ids name
     * the effect each slot gets, scaled by depth; the effects are not built yet. Each piece
     * uses its own icon (textures v3).
     */
    private static void miningArmor(ItemRegistry items) {
        armor(items, "miners_helmet", Rarity.COMMON, ArmorKind.HELMET, "mining_light_radius");
        armor(items, "miners_vest", Rarity.COMMON, ArmorKind.CHESTPLATE, "mining_damage_reduction");
        armor(items, "miners_trousers", Rarity.COMMON, ArmorKind.LEGGINGS, "mining_sneak_speed");
        armor(items, "miners_boots", Rarity.COMMON, ArmorKind.BOOTS, "mining_fall_reduction");
        armor(items, "cavers_helmet", Rarity.UNCOMMON, ArmorKind.HELMET, "mining_light_radius");
        armor(items, "reinforced_chestplate", Rarity.UNCOMMON, ArmorKind.CHESTPLATE, "mining_damage_reduction");
        armor(items, "cavers_leggings", Rarity.UNCOMMON, ArmorKind.LEGGINGS, "mining_sneak_speed");
        armor(items, "cavers_boots", Rarity.UNCOMMON, ArmorKind.BOOTS, "mining_fall_reduction");
        armor(items, "drillers_helm", Rarity.RARE, ArmorKind.HELMET, "mining_light_radius");
        armor(items, "drillers_cuirass", Rarity.RARE, ArmorKind.CHESTPLATE, "mining_damage_reduction");
        armor(items, "drillers_leggings", Rarity.RARE, ArmorKind.LEGGINGS, "mining_sneak_speed");
        armor(items, "drillers_boots", Rarity.RARE, ArmorKind.BOOTS, "mining_fall_reduction");
        armor(items, "abyssal_helm", Rarity.EPIC, ArmorKind.HELMET, "mining_light_radius");
        armor(items, "abyssal_cuirass", Rarity.EPIC, ArmorKind.CHESTPLATE, "mining_damage_reduction");
        armor(items, "abyssal_leggings", Rarity.EPIC, ArmorKind.LEGGINGS, "mining_sneak_speed");
        armor(items, "abyssal_boots", Rarity.EPIC, ArmorKind.BOOTS, "mining_fall_reduction");
    }

    private static void armor(ItemRegistry items, String id, Rarity rarity, ArmorKind kind, String trait) {
        // The worn layer is one per set: rune:<job>_<rarity>. All armor here is Mining.
        String equipment = "rune:mining_" + rarity.name().toLowerCase(java.util.Locale.ROOT);
        items.register(RuneItem.armor(
                id, rarity, id, new ArmorSpec(kind, null, null, null, null, null, trait).withEquipment(equipment)));
    }

    private static void component(ItemRegistry items, String id, Rarity rarity) {
        items.register(new RuneItem(id, rarity, Material.PAPER, id));
    }
}
