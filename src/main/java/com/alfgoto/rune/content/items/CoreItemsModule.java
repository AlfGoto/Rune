package com.alfgoto.rune.content.items;

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
import com.alfgoto.rune.text.Rarity;
import com.google.auto.service.AutoService;
import java.util.Set;
import org.bukkit.Material;

/** Registers the core items into the ItemRegistry. For now only the test item and the test pickaxe. */
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
        // Common helmet: leather protection, 1 slot, Capacity 3. No armor texture exists yet, so it uses the
        // test model (placeholder).
        items.register(RuneItem.armor("test_helmet", Rarity.COMMON, "test_rune", ArmorSpec.of(ArmorKind.HELMET)));
        // The Rune Codex (soulbound, opens the menus). Placeholder model until the visuals polish pass.
        items.register(new RuneItem(Codex.ID, Rarity.COMMON, Material.BOOK, "test_rune"));
        // Mining pickaxes (docs/jobs/mining.md). Common = stone tier, Capacity 3. The Trait is only an id for now
        // (Traits are not built); the "mines slower / faster" and durability numbers are not decided, so
        // speed and durability stay at the stone-tier default. Coal's Capacity is 3 - 1 = 2.
        items.register(RuneItem.tool("flint_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "flint_pickaxe",
                new ToolSpec(0, null, null, null, null, "flint_pickaxe")));
        items.register(RuneItem.tool("coal_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "coal_pickaxe",
                new ToolSpec(0, null, null, null, 2, "coal_pickaxe")));
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

    private static void component(ItemRegistry items, String id, Rarity rarity) {
        items.register(new RuneItem(id, rarity, Material.PAPER, id));
    }
}
