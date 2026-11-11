package com.alfgoto.rune.foundations.blocktracking;

import com.alfgoto.rune.command.Subcommand;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug blocks}: shows the Block Tracking tags of the block you look at. */
public final class BlocksDebug implements Subcommand {

    private static final int REACH = 10;

    private final BlockTracker tracker;

    public BlocksDebug(BlockTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public String name() {
        return "blocks";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable(
                            "rune.block_tracking.debug.player_only", "Only a player can do this.")
                    .color(NamedTextColor.RED));
            return;
        }
        Block block = player.getTargetBlockExact(REACH);
        if (block == null) {
            sender.sendMessage(Component.translatable(
                            "rune.block_tracking.debug.none", "You are not looking at a block.")
                    .color(NamedTextColor.RED));
            return;
        }
        Component type = Component.text(block.getType().name());
        Component position = Component.text(block.getX() + " " + block.getY() + " " + block.getZ());
        var tags = tracker.tags(block);
        if (tags.isEmpty()) {
            sender.sendMessage(Component.translatable(
                    "rune.block_tracking.debug.untagged", "%s at %s: no tags", type, position));
            return;
        }
        String ids = tags.stream().map(BlockTag::id).collect(Collectors.joining(", "));
        sender.sendMessage(Component.translatable(
                "rune.block_tracking.debug.tags", "%s at %s: %s", type, position, Component.text(ids)));
    }
}
