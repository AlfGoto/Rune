package com.alfgoto.rune.gameplay.traits;

import com.alfgoto.rune.command.Subcommand;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** {@code /rune debug copper_stage <0-3>}: sets the oxidation stage of the Copper tool in the main hand. */
final class CopperDebug implements Subcommand {

    private final CopperOxidation copper;

    CopperDebug(CopperOxidation copper) {
        this.copper = copper;
    }

    @Override
    public String name() {
        return "copper_stage";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
            return;
        }
        int stage = -1;
        if (args.size() == 1) {
            try {
                stage = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                stage = -1;
            }
        }
        if (stage < 0 || stage > CopperOxidation.LAST_STAGE) {
            player.sendMessage(error("rune.command.copper_stage.usage", "Usage: /rune debug copper_stage <0-3>"));
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!copper.setStage(tool, stage)) {
            player.sendMessage(error("rune.command.copper_stage.not_copper", "Hold a Copper tool in your main hand."));
            return;
        }
        player.sendMessage(Component.translatable(
                "rune.command.copper_stage.done",
                "Copper stage set to %s.",
                Component.text(stage)));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        return args.size() == 1 ? List.of("0", "1", "2", "3") : List.of();
    }

    private static Component error(String key, String fallback) {
        return Component.translatable(key, fallback).color(NamedTextColor.RED);
    }
}
