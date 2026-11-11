package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.foundations.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune level set <job> <level>}: sets your own Job Level. */
public final class LevelSubcommand implements Subcommand {

    private static final String USAGE_FALLBACK = "Usage: /rune level set <job> <level>";

    private final Registry<Job> jobs;
    private final JobProgress progress;

    public LevelSubcommand(Registry<Job> jobs, JobProgress progress) {
        this.jobs = jobs;
        this.progress = progress;
    }

    @Override
    public String name() {
        return "level";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() != 3 || !args.get(0).equalsIgnoreCase("set")) {
            sender.sendMessage(XpSubcommand.error("rune.command.level.usage", USAGE_FALLBACK));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(XpSubcommand.error("rune.command.player_only", "Only a player can do this."));
            return;
        }
        Job job = jobs.find(args.get(1).toLowerCase(Locale.ROOT)).orElse(null);
        if (job == null) {
            sender.sendMessage(XpSubcommand.error(
                    "rune.command.unknown_job", "Unknown Job: %s", Component.text(args.get(1))));
            return;
        }
        Integer level = Numbers.parseLevel(args.get(2));
        if (level == null) {
            sender.sendMessage(XpSubcommand.error(
                    "rune.command.invalid_number", "Invalid number: %s", Component.text(args.get(2))));
            return;
        }
        progress.setJobLevel(player.getUniqueId(), job, level);
        sender.sendMessage(Component.translatable(
                "rune.command.level.changed",
                "%s is now level %s",
                Component.translatable(job.translationKey(), job.fallbackName()),
                Component.text(level)));
    }

    @Override
    public List<String> complete(CommandSender sender, List<String> args) {
        List<String> out = new ArrayList<>();
        if (args.size() == 1 && "set".startsWith(args.get(0).toLowerCase(Locale.ROOT))) {
            out.add("set");
        } else if (args.size() == 2) {
            String typed = args.get(1).toLowerCase(Locale.ROOT);
            for (Job job : jobs.all()) {
                if (job.id().startsWith(typed)) {
                    out.add(job.id());
                }
            }
        }
        return out;
    }
}
