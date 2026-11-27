package com.alfgoto.rune.gameplay.blueprints;

import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.items.GiveVariant;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** {@code /rune give blueprint <id>}: gives yourself a Blueprint. */
final class GiveBlueprintVariant implements GiveVariant {

    private final Blueprints blueprints;

    GiveBlueprintVariant(Blueprints blueprints) {
        this.blueprints = blueprints;
    }

    @Override
    public String keyword() {
        return "blueprint";
    }

    @Override
    public void execute(Player player, List<String> args) {
        if (args.size() != 1) {
            player.sendMessage(error("rune.command.give_blueprint.usage", "Usage: /rune give blueprint <id>"));
            return;
        }
        Blueprint blueprint = blueprints.find(args.get(0).toLowerCase(Locale.ROOT)).orElse(null);
        if (blueprint == null) {
            player.sendMessage(error(
                    "rune.command.give_blueprint.unknown", "Unknown Blueprint: %s", Component.text(args.get(0))));
            return;
        }
        GiveSubcommand.give(player, blueprints.create(blueprint));
        player.sendMessage(Component.translatable(
                "rune.command.give_blueprint.done",
                "Gave the Blueprint: %s.",
                Component.translatable(blueprint.translationKey(), blueprint.fallbackName())));
    }

    @Override
    public List<String> complete(List<String> args) {
        if (args.size() != 1) {
            return List.of();
        }
        String typed = args.get(0).toLowerCase(Locale.ROOT);
        return blueprints.all().stream().map(Blueprint::id).filter(id -> id.startsWith(typed)).toList();
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
