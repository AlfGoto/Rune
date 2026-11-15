package com.alfgoto.rune.gameplay.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import java.util.List;
import net.kyori.adventure.util.TriState;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemStack;

/**
 * Puts a Rune Tool's stats on its stack with vanilla components, so the game itself mines at that
 * Harvest Tier and speed: the {@code tool} component (same two rules as vanilla tools) and
 * {@code max_damage}.
 */
final class ToolComponents {

    private ToolComponents() {}

    static void apply(ItemStack stack, ToolStats stats) {
        // Same order as vanilla: blocks above the tier give no drops, then the kind's blocks mine at speed.
        Tool tool = Tool.tool()
                .defaultMiningSpeed(1.0f)
                .damagePerBlock(1)
                .addRule(Tool.rule(blocks(stats.tier().incorrectBlocks()), null, TriState.FALSE))
                .addRule(Tool.rule(blocks(stats.kind().mineable()), stats.speed(), TriState.TRUE))
                .build();
        stack.setData(DataComponentTypes.TOOL, tool);
        stack.setData(DataComponentTypes.MAX_DAMAGE, stats.durability());
    }

    private static RegistryKeySet<BlockType> blocks(Tag<Material> tag) {
        List<BlockType> types = tag.getValues().stream().map(Material::asBlockType).toList();
        return RegistrySet.keySetFromValues(RegistryKey.BLOCK, types);
    }
}
