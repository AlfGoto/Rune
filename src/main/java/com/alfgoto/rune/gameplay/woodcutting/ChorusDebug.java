package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.command.Subcommand;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug chorus}: shows in chat why each hit did or did not teleport you (Chorus Axe). */
final class ChorusDebug implements Subcommand {

    private final HurtTeleportListener listener;

    ChorusDebug(HurtTeleportListener listener) {
        this.listener = listener;
    }

    @Override
    public String name() {
        return "chorus";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("rune.reveal.player_only", "Only a player can do this.")
                    .color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(listener.toggleDebug(player)
                ? Component.translatable("rune.debug.chorus.on", "Chorus debug: on")
                : Component.translatable("rune.debug.chorus.off", "Chorus debug: off"));
    }
}
