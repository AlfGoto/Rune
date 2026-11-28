package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.foundations.module.RuneContext;
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
import com.alfgoto.rune.gameplay.runes.HeldTraits;
import com.alfgoto.rune.gameplay.woodcutting.AxeTraits;
import com.alfgoto.rune.gameplay.woodcutting.LeafRules;
import com.alfgoto.rune.gameplay.woodcutting.ReplantRules;
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
        startPickaxes(items, ctx.config());
        startAxes(items, ctx.config());
        biomePickaxes(items, ctx.config().tools());
        epicPickaxes(items, ctx.config());
        woodcuttingAxes(items, ctx.config());
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
     * The Obsidian and Tunneler Pickaxes (docs/jobs/mining.md): Epic, diamond tier. Obsidian is nearly unbreakable
     * (a big Sturdy value) and slow, Capacity +3. Tunneler mines 3x3 (done by {@link TunnelerListener}), is slower
     * and wears faster (every block of the 3x3 costs durability), Capacity -3. Numbers: config
     * {@code traits.obsidian_pickaxe} and {@code traits.tunneler}.
     */
    private static void epicPickaxes(ItemRegistry items, RuneConfig config) {
        double sturdy = config.traitNumber("obsidian_pickaxe", "sturdy_percent", 95);
        double obsidianSlower = config.traitNumber("obsidian_pickaxe", "slower_percent", 20);
        items.register(RuneItem.tool("obsidian_pickaxe", Rarity.EPIC, ToolKind.PICKAXE, "obsidian_pickaxe",
                ToolSpec.DEFAULT.withTrait("obsidian_pickaxe", 3,
                        TraitBinding.always("sturdy", sturdy), TraitBinding.always("haste", -obsidianSlower))));
        double tunnelerSlower = config.traitNumber("tunneler", "slower_percent", 20);
        items.register(RuneItem.tool("tunneler", Rarity.EPIC, ToolKind.PICKAXE, "tunneler",
                ToolSpec.DEFAULT.withTrait("tunneler", -3,
                        TraitBinding.always(TunnelerListener.EFFECT_ID, 1) /* on/off flag */,
                        TraitBinding.always("haste", -tunnelerSlower))));
    }

    /**
     * The Birch, Spruce, Poplar and Jungle axes (docs/jobs/woodcutting.md). Each has the Affinity Trait of its
     * Place (the value of an Affinity Rune at Rank 1) and one extra. Tier and Capacity follow the Rarity.
     */
    private static void woodcuttingAxes(ItemRegistry items, RuneConfig config) {
        double affinity = config.speedRunes().affinityPerRank();
        double birchSeconds = config.traitNumber(AxeTraits.FELLED_TREE_SPEED, "seconds", 5);
        double climbPercent = config.traitNumber(AxeTraits.CLIMB_SPEED, "percent", 50);
        axe(items, "birch_axe", Rarity.UNCOMMON,
                TraitBinding.always("affinity_birch_forest", affinity),
                TraitBinding.always(AxeTraits.FELLED_TREE_SPEED, birchSeconds));
        axe(items, "spruce_axe", Rarity.UNCOMMON,
                TraitBinding.always("affinity_taiga", affinity),
                TraitBinding.always(AxeTraits.POWDER_SNOW_IMMUNITY, 1));
        axe(items, "poplar_axe", Rarity.UNCOMMON,
                TraitBinding.always("affinity_dappled_forest", affinity),
                TraitBinding.always(AxeTraits.FELLED_TREE_LEAF_FALL, 1));
        axe(items, "jungle_axe", Rarity.RARE,
                TraitBinding.always("affinity_jungle", affinity),
                TraitBinding.always(AxeTraits.CLIMB_SPEED, climbPercent));
        harvestAxes(items, config);
    }

    /**
     * The harvest line (docs/jobs/woodcutting.md): Orchard (more apples and saplings), Golden Bough (golden
     * apples from oak leaves) and Verdant (replants a sapling). Numbers in config
     * ({@code traits.orchard_axe}, {@code traits.golden_bough}). Tier and Capacity follow the Rarity.
     */
    private static void harvestAxes(ItemRegistry items, RuneConfig config) {
        double orchard = config.traitNumber("orchard_axe", "orchard_percent", 1000);
        double golden = config.traitNumber("golden_bough", "golden_apple_percent", 5);
        double enchanted = config.traitNumber("golden_bough", "enchanted_golden_apple_percent", 0.1);
        axe(items, "orchard_axe", Rarity.UNCOMMON, TraitBinding.always(LeafRules.ORCHARD, orchard));
        axe(
                items,
                "golden_bough",
                Rarity.RARE,
                TraitBinding.always(LeafRules.GOLDEN_APPLE, golden),
                TraitBinding.always(LeafRules.ENCHANTED_GOLDEN_APPLE, enchanted));
        axe(items, "verdant_axe", Rarity.EPIC, TraitBinding.always(ReplantRules.REPLANT, 1));
    }

    /**
     * The Flint, Coal and Ironbound Pickaxes (docs/jobs/mining.md). Numbers are in config
     * ({@code traits.flint_pickaxe}, {@code traits.coal_pickaxe}, {@code traits.ironbound_pickaxe}).
     */
    private static void startPickaxes(ItemRegistry items, RuneConfig config) {
        // Flint: Common, stone tier, Capacity 3. Fortune, mines slower.
        double fortune = config.traitNumber("flint_pickaxe", "fortune", 15);
        double slower = config.traitNumber("flint_pickaxe", "slower_percent", 10);
        items.register(RuneItem.tool("flint_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "flint_pickaxe",
                ToolSpec.DEFAULT.withTrait("flint_pickaxe", 0,
                        TraitBinding.always("fortune", fortune), TraitBinding.always("haste", -slower))));
        // Coal: Common, stone tier, Capacity 3 -1. Mines faster, lower durability.
        double faster = config.traitNumber("coal_pickaxe", "faster_percent", 15);
        int coalDurability = (int) config.traitNumber("coal_pickaxe", "durability", 100);
        items.register(RuneItem.tool("coal_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "coal_pickaxe",
                withDurability(ToolSpec.DEFAULT.withTrait("coal_pickaxe", -1, TraitBinding.always("haste", faster)),
                        coalDurability)));
        // Ironbound: Uncommon, iron tier, Capacity 6 +2. Plain, very durable.
        int ironboundDurability = (int) config.traitNumber("ironbound_pickaxe", "durability", 500);
        items.register(RuneItem.tool("ironbound_pickaxe", Rarity.UNCOMMON, ToolKind.PICKAXE, "ironbound_pickaxe",
                withDurability(ToolSpec.DEFAULT.withTrait("ironbound_pickaxe", 2), ironboundDurability)));
    }

    /**
     * The Flint Hatchet, Stone Hatchet and Woodsman's Axe (docs/jobs/woodcutting.md). Numbers are in config
     * ({@code traits.flint_hatchet}, {@code traits.stone_hatchet}, {@code traits.woodsmans_axe}).
     */
    private static void startAxes(ItemRegistry items, RuneConfig config) {
        // Flint Hatchet: Common, stone tier, Capacity 3. Chance of +1 log (Fortune), mines slower.
        double fortune = config.traitNumber("flint_hatchet", "fortune", 15);
        double slower = config.traitNumber("flint_hatchet", "slower_percent", 10);
        items.register(RuneItem.tool("flint_hatchet", Rarity.COMMON, ToolKind.AXE, "flint_hatchet",
                ToolSpec.DEFAULT.withTrait("flint_hatchet", 0,
                        TraitBinding.always("fortune", fortune), TraitBinding.always("haste", -slower))));
        // Stone Hatchet: Common, stone tier, Capacity 3 -1. Faster, less durable.
        double faster = config.traitNumber("stone_hatchet", "faster_percent", 15);
        int stoneDurability = (int) config.traitNumber("stone_hatchet", "durability", 100);
        items.register(RuneItem.tool("stone_hatchet", Rarity.COMMON, ToolKind.AXE, "stone_hatchet",
                withDurability(ToolSpec.DEFAULT.withTrait("stone_hatchet", -1, TraitBinding.always("haste", faster)),
                        stoneDurability)));
        // Woodsman's Axe: Uncommon, iron tier, Capacity 6 +2. Plain, very durable.
        int woodsmanDurability = (int) config.traitNumber("woodsmans_axe", "durability", 500);
        items.register(RuneItem.tool("woodsmans_axe", Rarity.UNCOMMON, ToolKind.AXE, "woodsmans_axe",
                withDurability(ToolSpec.DEFAULT.withTrait("woodsmans_axe", 2), woodsmanDurability)));
    }

    private static ToolSpec withDurability(ToolSpec spec, int durability) {
        return new ToolSpec(spec.tierOffset(), spec.speed(), durability, spec.slots(), spec.capacity(), spec.trait(),
                spec.traitBindings(), spec.capacityModifier());
    }

    private static void axe(ItemRegistry items, String id, Rarity rarity, TraitBinding... bindings) {
        items.register(RuneItem.tool(id, rarity, ToolKind.AXE, id, ToolSpec.DEFAULT.withTrait(id, 0, bindings)));
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
