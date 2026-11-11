package com.alfgoto.rune.command;

import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.foundations.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /rune xp [player]} shows Job XP and Account XP.
 * {@code /rune xp set|add <job> <amount>} changes your own Job XP.
 */
public final class XpSubcommand implements Subcommand {

    private final Registry<Job> jobs;
    private final JobProgress progress;

    public XpSubcommand(Registry<Job> jobs, JobProgress progress) {
        this.jobs = jobs;
        this.progress = progress;
    }

    @Override
    public String name() {
        return "xp";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!args.isEmpty() && (args.get(0).equalsIgnoreCase("set") || args.get(0).equalsIgnoreCase("add"))) {
            change(sender, args);
        } else if (args.size() <= 1) {
            show(sender, args);
        } else {
            sender.sendMessage(error("rune.command.xp.usage", "Usage: /rune xp [player] | /rune xp <set|add> <job> <amount>"));
        }
    }

    private void show(CommandSender sender, List<String> args) {
        Player target;
        if (args.isEmpty()) {
            if (!(sender instanceof Player self)) {
                sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
                return;
            }
            target = self;
        } else {
            target = Bukkit.getPlayerExact(args.get(0));
            if (target == null) {
                sender.sendMessage(error(
                        "rune.command.player_not_found", "Player not found: %s", Component.text(args.get(0))));
                return;
            }
        }
        var id = target.getUniqueId();
        sender.sendMessage(Component.translatable(
                        "rune.command.xp.header", "Job XP of %s", Component.text(target.getName()))
                .color(NamedTextColor.GOLD));
        for (Job job : jobs.all()) {
            int level = progress.jobLevel(id, job);
            sender.sendMessage(Component.translatable(
                    "rune.command.xp.job",
                    "%s: level %s (%s / %s XP)",
                    Component.translatable(job.translationKey(), job.fallbackName()),
                    Component.text(level),
                    Component.text(Numbers.format(progress.jobXp(id, job))),
                    Component.text(Numbers.format(progress.jobCurve().totalXpForLevel(level + 1)))));
        }
        int accountLevel = progress.accountLevel(id);
        sender.sendMessage(Component.translatable(
                "rune.command.xp.account",
                "Account: level %s (%s / %s XP)",
                Component.text(accountLevel),
                Component.text(Numbers.format(progress.accountXp(id))),
                Component.text(Numbers.format(progress.accountCurve().totalXpForLevel(accountLevel + 1)))));
    }

    private void change(CommandSender sender, List<String> args) {
        if (args.size() != 3) {
            sender.sendMessage(error("rune.command.xp.usage", "Usage: /rune xp [player] | /rune xp <set|add> <job> <amount>"));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(error("rune.command.player_only", "Only a player can use this."));
            return;
        }
        Job job = jobs.find(args.get(1).toLowerCase(Locale.ROOT)).orElse(null);
        if (job == null) {
            sender.sendMessage(error("rune.command.unknown_job", "Unknown Job: %s", Component.text(args.get(1))));
            return;
        }
        Double amount = Numbers.parseXp(args.get(2));
        if (amount == null) {
            sender.sendMessage(error("rune.command.invalid_number", "Invalid number: %s", Component.text(args.get(2))));
            return;
        }
        var id = player.getUniqueId();
        if (args.get(0).equalsIgnoreCase("set")) {
            progress.setJobXp(id, job, amount);
        } else {
            progress.addJobXp(id, job, amount);
        }
        sender.sendMessage(Component.translatable(
                "rune.command.xp.changed",
                "%s XP is now %s (Account XP: %s)",
                Component.translatable(job.translationKey(), job.fallbackName()),
                Component.text(Numbers.format(progress.jobXp(id, job))),
                Component.text(Numbers.format(progress.accountXp(id)))));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        List<String> out = new ArrayList<>();
        if (args.size() == 1) {
            String typed = args.get(0).toLowerCase(Locale.ROOT);
            for (String word : List.of("set", "add")) {
                if (word.startsWith(typed)) {
                    out.add(word);
                }
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(typed)) {
                    out.add(p.getName());
                }
            }
        } else if (args.size() == 2 && (args.get(0).equalsIgnoreCase("set") || args.get(0).equalsIgnoreCase("add"))) {
            String typed = args.get(1).toLowerCase(Locale.ROOT);
            for (Job job : jobs.all()) {
                if (job.id().startsWith(typed)) {
                    out.add(job.id());
                }
            }
        }
        return out;
    }

    static Component error(String key, String fallback, Component... args) {
        return Component.translatable(key, fallback, args).color(NamedTextColor.RED);
    }
}
