package com.alfgoto.rune.command;

import com.alfgoto.rune.foundations.blocktracking.BlockTag;
import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug block}: shows the Block Tracking tags of the block you look at. */
public final class DebugSubcommand implements Subcommand {

    private static final String BLOCK = "block";
    private static final int REACH = 10;

    private final BlockTracker tracker;

    public DebugSubcommand(BlockTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public String name() {
        return "debug";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty() || !args.get(0).toLowerCase(Locale.ROOT).equals(BLOCK)) {
            sender.sendMessage(Component.translatable(
                            "rune.command.debug.usage", "Usage: /rune debug <%s>", Component.text(BLOCK))
                    .color(NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("rune.command.player_only", "Only a player can do this.")
                    .color(NamedTextColor.RED));
            return;
        }
        Block block = player.getTargetBlockExact(REACH);
        if (block == null) {
            sender.sendMessage(Component.translatable(
                            "rune.command.debug.block.none", "You are not looking at a block.")
                    .color(NamedTextColor.RED));
            return;
        }
        Component type = Component.text(block.getType().name());
        Component position = Component.text(block.getX() + " " + block.getY() + " " + block.getZ());
        var tags = tracker.tags(block);
        if (tags.isEmpty()) {
            sender.sendMessage(Component.translatable(
                    "rune.command.debug.block.untagged", "%s at %s: no tags", type, position));
            return;
        }
        String ids = tags.stream().map(BlockTag::id).collect(Collectors.joining(", "));
        sender.sendMessage(Component.translatable(
                "rune.command.debug.block.tags", "%s at %s: %s", type, position, Component.text(ids)));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        return args.size() == 1 && BLOCK.startsWith(args.get(0).toLowerCase(Locale.ROOT)) ? List.of(BLOCK) : List.of();
    }
}
