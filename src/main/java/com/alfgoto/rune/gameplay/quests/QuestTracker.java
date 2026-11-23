package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.Numbers;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.LootRoller;
import com.alfgoto.rune.gameplay.rewards.LootTable;
import com.alfgoto.rune.gameplay.rewards.LootTableRegistry;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Text;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Counts Quest progress and completes Quests. Each {@link JobActionEvent} adds 1 to every active Quest of
 * that Job (a Quest Type's {@link QuestObjective} can ignore some, e.g. wrong block or wrong tool). A
 * finished Quest gives Job XP and an Account XP bonus (config {@code quests.reward-*}) and the Drops of a
 * roll of the {@value #LOOT_TABLE} loot table at the Quest Tier, leaning to the Quest's Job.
 * Then the slot gets a new Quest.
 */
public final class QuestTracker implements Listener {

    /** The loot table a completed Quest rolls. Drops are given directly (no sealed Rune Cache yet). */
    public static final String LOOT_TABLE = "cache";

    private static final Key COMPLETE_SOUND = Key.key("minecraft", "ui.toast.challenge_complete");

    private final QuestBook book;
    private final JobProgress jobProgress;
    private final RuneConfig config;
    private final LootTableRegistry tables;
    private final LootRoller roller;
    private final RandomGenerator random;

    public QuestTracker(
            QuestBook book,
            JobProgress jobProgress,
            RuneConfig config,
            LootTableRegistry tables,
            LootRoller roller,
            RandomGenerator random) {
        this.book = book;
        this.jobProgress = jobProgress;
        this.config = config;
        this.tables = tables;
        this.roller = roller;
        this.random = random;
    }

    @EventHandler
    public void onJobAction(JobActionEvent event) {
        Player player = event.player();
        UUID id = player.getUniqueId();
        for (var entry : book.slots(id).entrySet()) {
            int slot = entry.getKey();
            Quest quest = entry.getValue();
            if (!quest.type().job().equals(event.job()) || !quest.type().objective().counts(event)) {
                continue;
            }
            int done = book.advance(id, slot);
            if (done >= quest.target()) {
                complete(player, slot, quest);
            } else {
                Bukkit.getPluginManager().callEvent(new QuestProgressEvent(player, quest, done));
            }
        }
    }

    /** Rewards the finished Quest of {@code slot} and draws a new one. */
    void complete(Player player, int slot, Quest quest) {
        Job job = quest.type().job();
        int tier = quest.tier().ordinal();
        double jobXp = config.questRewardJobXp().get(tier);
        double accountXp = config.questRewardAccountXp().get(tier);
        // Replace first, so the slot is never left finished if a reward fails.
        Optional<Quest> next = book.replace(player.getUniqueId(), slot);
        jobProgress.addJobXp(player.getUniqueId(), job, jobXp);
        jobProgress.addAccountXpBonus(player.getUniqueId(), accountXp);
        List<Drop> drops = rollDrops(player, quest);
        for (Drop drop : drops) {
            drop.give(player);
        }
        player.sendMessage(Component.translatable(
                        "rune.quest.complete",
                        "Quest complete: %s - %s (%s)",
                        Component.translatable(job.translationKey(), job.fallbackName()),
                        Component.translatable(quest.type().translationKey(), quest.type().fallbackName()),
                        Text.rarityName(quest.tier()))
                .color(NamedTextColor.GOLD));
        player.sendMessage(Component.translatable(
                        "rune.quest.reward",
                        "Rewards: +%s %s XP, +%s Account XP",
                        Component.text(Numbers.format(jobXp)),
                        Component.translatable(job.translationKey(), job.fallbackName()),
                        Component.text(Numbers.format(accountXp)))
                .color(NamedTextColor.GREEN));
        for (Drop drop : drops) {
            player.sendMessage(Component.text("- ", NamedTextColor.GRAY).append(drop.describe()));
        }
        next.ifPresent(q -> player.sendMessage(QuestSubcommand.describe(
                "rune.quest.new", "New Quest %s: %s - %s (%s): %s", slot, q)));
        player.playSound(Sound.sound(COMPLETE_SOUND, Sound.Source.PLAYER, 1f, 1f));
    }

    private List<Drop> rollDrops(Player player, Quest quest) {
        Optional<LootTable> table = tables.registry().find(LOOT_TABLE);
        if (table.isEmpty()) {
            return List.of();
        }
        RollContext context = new RollContext(player.getUniqueId(), quest.tier(), quest.type().job().id());
        return roller.roll(table.get(), context, random);
    }
}
