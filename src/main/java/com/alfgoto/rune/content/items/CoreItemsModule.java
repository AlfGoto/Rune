package com.alfgoto.rune.content.items;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
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
        items.register(RuneItem.tool("test_pickaxe", Rarity.COMMON, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        // The Rune Codex (soulbound, opens the menus). Placeholder model until the visuals polish pass.
        items.register(new RuneItem(Codex.ID, Rarity.COMMON, Material.BOOK, "test_rune"));
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
