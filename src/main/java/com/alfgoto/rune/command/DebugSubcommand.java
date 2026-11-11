package com.alfgoto.rune.command;

import com.alfgoto.rune.visuals.reveal.BlockReveal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug}: tools to test visuals in game. */
public final class DebugSubcommand implements Subcommand {

    static final int MAX_RADIUS = 32;
    static final int REVEAL_TICKS = 200;

    private final BlockReveal reveal;

    public DebugSubcommand(BlockReveal reveal) {
        this.reveal = reveal;
    }

    @Override
    public String name() {
        return "debug";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() == 2 && args.get(0).equalsIgnoreCase("reveal")) {
            revealOres(sender, args.get(1));
            return;
        }
        sender.sendMessage(Component.translatable("rune.command.debug.usage", "Usage: /rune debug reveal <radius>")
                .color(NamedTextColor.RED));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        if (args.size() == 1 && "reveal".startsWith(args.get(0).toLowerCase(Locale.ROOT))) {
            return List.of("reveal");
        }
        return List.of();
    }

    private void revealOres(CommandSender sender, String radiusText) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("rune.command.player_only", "Only a player can do this.")
                    .color(NamedTextColor.RED));
            return;
        }
        int radius;
        try {
            radius = Integer.parseInt(radiusText);
        } catch (NumberFormatException e) {
            radius = -1;
        }
        if (radius < 1 || radius > MAX_RADIUS) {
            sender.sendMessage(Component.translatable(
                            "rune.command.debug.bad_radius",
                            "The radius must be a number from 1 to %s.",
                            Component.text(MAX_RADIUS))
                    .color(NamedTextColor.RED));
            return;
        }
        List<Block> ores = findOres(player, radius);
        reveal.reveal(player, ores, REVEAL_TICKS, Color.AQUA);
        sender.sendMessage(Component.translatable(
                "rune.command.debug.revealed",
                "Outlined %s ore blocks for %s seconds.",
                Component.text(ores.size()),
                Component.text(REVEAL_TICKS / 20)));
    }

    private static List<Block> findOres(Player player, int radius) {
        Block center = player.getLocation().getBlock();
        List<Block> ores = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (isOre(block.getType())) {
                        ores.add(block);
                    }
                }
            }
        }
        return ores;
    }

    private static boolean isOre(Material material) {
        return material.name().endsWith("_ORE") || material == Material.ANCIENT_DEBRIS;
    }
}
