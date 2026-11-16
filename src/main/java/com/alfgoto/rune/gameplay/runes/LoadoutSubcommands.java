package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.command.Subcommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Debug commands on the held Rune Tool: {@code /rune debug install <rune> [rank]} and {@code /rune debug uninstall <slot>}. */
final class LoadoutSubcommands {

    private LoadoutSubcommands() {}

    static Subcommand install(ToolLoadout loadout, RuneRegistry runes) {
        return new Subcommand() {
            @Override
            public String name() {
                return "install";
            }

            @Override
            public void execute(CommandSender sender, List<String> args) {
                Player player = player(sender);
                if (player == null) {
                    return;
                }
                if (args.isEmpty() || args.size() > 2) {
                    player.sendMessage(error("rune.command.install.usage", "Usage: /rune debug install <rune> [rank]"));
                    return;
                }
                Rune rune = runes.find(args.get(0).toLowerCase(Locale.ROOT)).orElse(null);
                if (rune == null) {
                    player.sendMessage(
                            error("rune.command.install.unknown", "Unknown Rune: %s", Component.text(args.get(0))));
                    return;
                }
                int rank = 1;
                if (args.size() == 2) {
                    try {
                        rank = Integer.parseInt(args.get(1));
                    } catch (NumberFormatException e) {
                        rank = 0;
                    }
                }
                ItemStack tool = player.getInventory().getItemInMainHand();
                ToolLoadout.Result result = loadout.install(tool, rune, rank);
                player.sendMessage(switch (result) {
                    case INSTALLED -> Component.translatable(
                            "rune.command.install.done",
                            "Installed %s, Rank %s. Capacity: %s / %s.",
                            runeName(rune),
                            Component.text(rank),
                            Component.text(loadout.used(tool)),
                            Component.text(loadout.capacity(tool)));
                    case NOT_A_TOOL -> error(
                            "rune.command.loadout.not_a_tool", "Hold a Rune Tool or Rune Armor in your main hand.");
                    case DOES_NOT_FIT -> error(
                            "rune.command.install.no_fit", "%s does not fit in this item.", runeName(rune));
                    case BAD_RANK -> rune.maxRank().isPresent()
                            ? error(
                                    "rune.command.give_rune.bad_rank",
                                    "The Rank must be a number from 1 to %s.",
                                    Component.text(rune.maxRank().getAsInt()))
                            : error("rune.command.give_rune.bad_rank_min", "The Rank must be 1 or more.");
                    case NO_FREE_SLOT -> error("rune.command.install.no_slot", "No free Rune slot on this item.");
                    case OVER_CAPACITY -> error(
                            "rune.command.install.over_capacity",
                            "Not enough Capacity: this Rune costs %s, %s left.",
                            Component.text(runes.capacityCost(rune, rank)),
                            Component.text(loadout.capacity(tool) - loadout.used(tool)));
                });
            }

            @Override
            public List<String> complete(CommandSender sender, List<String> args) {
                if (args.size() != 1) {
                    return List.of();
                }
                String typed = args.get(0).toLowerCase(Locale.ROOT);
                List<String> ids = new ArrayList<>();
                for (Rune rune : runes.all()) {
                    if (rune.id().startsWith(typed)) {
                        ids.add(rune.id());
                    }
                }
                return ids;
            }
        };
    }

    /** {@code /rune install player <rune> [rank]}: installs a Rune on the player. Does not consume a Rune item yet. */
    static Subcommand installPlayer(PlayerLoadout loadout, RuneRegistry runes) {
        return new Subcommand() {
            @Override
            public String name() {
                return "install";
            }

            @Override
            public void execute(CommandSender sender, List<String> args) {
                Player player = player(sender);
                if (player == null) {
                    return;
                }
                if (args.size() < 2 || args.size() > 3 || !args.get(0).equalsIgnoreCase("player")) {
                    player.sendMessage(error(
                            "rune.command.install_player.usage", "Usage: /rune install player <rune> [rank]"));
                    return;
                }
                Rune rune = runes.find(args.get(1).toLowerCase(Locale.ROOT)).orElse(null);
                if (rune == null) {
                    player.sendMessage(
                            error("rune.command.install.unknown", "Unknown Rune: %s", Component.text(args.get(1))));
                    return;
                }
                int rank = 1;
                if (args.size() == 3) {
                    try {
                        rank = Integer.parseInt(args.get(2));
                    } catch (NumberFormatException e) {
                        rank = 0;
                    }
                }
                UUID id = player.getUniqueId();
                ToolLoadout.Result result = loadout.install(id, rune, rank);
                player.sendMessage(switch (result) {
                    case INSTALLED -> Component.translatable(
                            "rune.command.install.done",
                            "Installed %s, Rank %s. Capacity: %s / %s.",
                            runeName(rune),
                            Component.text(rank),
                            Component.text(loadout.used(id)),
                            Component.text(loadout.capacity(id)));
                    case DOES_NOT_FIT, NOT_A_TOOL -> error(
                            "rune.command.install_player.no_fit",
                            "%s is not a passive Rune: it can't go on the player.",
                            runeName(rune));
                    case BAD_RANK -> rune.maxRank().isPresent()
                            ? error(
                                    "rune.command.give_rune.bad_rank",
                                    "The Rank must be a number from 1 to %s.",
                                    Component.text(rune.maxRank().getAsInt()))
                            : error("rune.command.give_rune.bad_rank_min", "The Rank must be 1 or more.");
                    case NO_FREE_SLOT -> error(
                            "rune.command.install_player.no_slot",
                            "All %s Player Rune slots are used.",
                            Component.text(PlayerLoadout.SLOTS));
                    case OVER_CAPACITY -> error(
                            "rune.command.install.over_capacity",
                            "Not enough Capacity: this Rune costs %s, %s left.",
                            Component.text(runes.capacityCost(rune, rank)),
                            Component.text(loadout.capacity(id) - loadout.used(id)));
                });
            }

            @Override
            public List<String> complete(CommandSender sender, List<String> args) {
                if (args.size() == 1) {
                    return "player".startsWith(args.get(0).toLowerCase(Locale.ROOT)) ? List.of("player") : List.of();
                }
                if (args.size() != 2) {
                    return List.of();
                }
                String typed = args.get(1).toLowerCase(Locale.ROOT);
                List<String> ids = new ArrayList<>();
                for (Rune rune : runes.all()) {
                    if (rune.fits().contains(Fit.PLAYER) && rune.id().startsWith(typed)) {
                        ids.add(rune.id());
                    }
                }
                return ids;
            }
        };
    }

    static Subcommand uninstall(ToolLoadout loadout) {
        return new Subcommand() {
            @Override
            public String name() {
                return "uninstall";
            }

            @Override
            public void execute(CommandSender sender, List<String> args) {
                Player player = player(sender);
                if (player == null) {
                    return;
                }
                ItemStack tool = player.getInventory().getItemInMainHand();
                if (loadout.stats(tool).isEmpty() && loadout.armorStats(tool).isEmpty()) {
                    player.sendMessage(error(
                            "rune.command.loadout.not_a_tool", "Hold a Rune Tool or Rune Armor in your main hand."));
                    return;
                }
                int slot = 0;
                if (args.size() == 1) {
                    try {
                        slot = Integer.parseInt(args.get(0));
                    } catch (NumberFormatException e) {
                        slot = 0;
                    }
                }
                if (slot < 1) {
                    player.sendMessage(error("rune.command.uninstall.usage", "Usage: /rune debug uninstall <slot>"));
                    return;
                }
                ToolLoadout.Installed removed = loadout.uninstall(tool, slot).orElse(null);
                if (removed == null) {
                    player.sendMessage(
                            error("rune.command.uninstall.empty", "Slot %s has no Rune.", Component.text(slot)));
                    return;
                }
                player.sendMessage(Component.translatable(
                        "rune.command.uninstall.done",
                        "Removed %s, Rank %s. Capacity: %s / %s.",
                        runeName(removed.rune()),
                        Component.text(removed.rank()),
                        Component.text(loadout.used(tool)),
                        Component.text(loadout.capacity(tool))));
            }
        };
    }

    private static Player player(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
        return null;
    }

    private static Component runeName(Rune rune) {
        return Component.translatable(rune.item().translationKey(), rune.item().fallbackName())
                .color(rune.rarity().color());
    }

    private static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
