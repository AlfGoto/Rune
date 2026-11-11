package com.alfgoto.rune.visuals.reveal;

import com.alfgoto.rune.command.Subcommand;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug reveal <radius>}: outlines ores around you to test Block Reveal. */
public final class RevealDebug implements Subcommand {

    static final int MAX_RADIUS = 32;
    static final int REVEAL_TICKS = 200;

    private final BlockReveal reveal;

    public RevealDebug(BlockReveal reveal) {
        this.reveal = reveal;
    }

    @Override
    public String name() {
        return "reveal";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            revealOres(sender, args.get(0));
            return;
        }
        sender.sendMessage(Component.translatable("rune.reveal.usage", "Usage: /rune debug reveal <radius>")
                .color(NamedTextColor.RED));
    }

    private void revealOres(CommandSender sender, String radiusText) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("rune.reveal.player_only", "Only a player can do this.")
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
                            "rune.reveal.bad_radius",
                            "The radius must be a number from 1 to %s.",
                            Component.text(MAX_RADIUS))
                    .color(NamedTextColor.RED));
            return;
        }
        List<Block> ores = findOres(player, radius);
        reveal.reveal(player, ores, REVEAL_TICKS, Color.AQUA);
        sender.sendMessage(Component.translatable(
                "rune.reveal.revealed",
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
