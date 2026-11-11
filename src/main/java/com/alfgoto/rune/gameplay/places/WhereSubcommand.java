package com.alfgoto.rune.gameplay.places;

import com.alfgoto.rune.command.Subcommand;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune where}: lists the Places the player is in. */
public final class WhereSubcommand implements Subcommand {

    private final PlaceDetector detector;

    public WhereSubcommand(PlaceDetector detector) {
        this.detector = detector;
    }

    @Override
    public String name() {
        return "where";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.translatable("rune.command.player_only", "Only a player can do this.")
                    .color(NamedTextColor.RED));
            return;
        }
        List<Place> places = detector.placesOf(player);
        if (places.isEmpty()) {
            sender.sendMessage(Component.translatable("rune.command.where.none", "You are not in any Place."));
            return;
        }
        sender.sendMessage(Component.translatable("rune.command.where.header", "You are in:"));
        for (Place place : places) {
            sender.sendMessage(Component.text("- ")
                    .append(Component.translatable(place.translationKey(), displayName(place.id()))));
        }
    }

    /** English fallback from the id: {@code lush_caves} becomes {@code Lush Caves}. */
    private static String displayName(String id) {
        StringBuilder name = new StringBuilder();
        for (String word : id.split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }
}
