package com.alfgoto.rune.visuals.feedback;

import com.alfgoto.rune.gameplay.jobs.JobXpGain;
import com.alfgoto.rune.gameplay.jobs.JobXpListener;
import java.time.Duration;
import java.util.Locale;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Shows Job XP gains: action bar, level-up title and sound, Account Level-up message. */
public final class XpFeedback implements JobXpListener {

    private static final Key LEVEL_UP_SOUND = Key.key("minecraft", "entity.player.levelup");

    @Override
    public void onJobXpGain(JobXpGain gain) {
        Player player = Bukkit.getPlayer(gain.player());
        if (player == null) {
            return;
        }
        Component jobName = Component.translatable(gain.job().translationKey(), gain.job().fallbackName());
        player.sendActionBar(Component.translatable(
                        "rune.feedback.xp_gain", "+%s %s XP", Component.text(format(gain.amount())), jobName)
                .color(NamedTextColor.GREEN));
        if (gain.jobLeveledUp()) {
            player.showTitle(Title.title(
                    Component.translatable("rune.feedback.level_up.title", "Level up!")
                            .color(NamedTextColor.GOLD),
                    Component.translatable(
                            "rune.feedback.level_up.subtitle",
                            "%s is now level %s",
                            jobName,
                            Component.text(gain.jobLevelAfter())),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))));
            player.playSound(Sound.sound(LEVEL_UP_SOUND, Sound.Source.PLAYER, 1f, 1f));
        }
        if (gain.accountLeveledUp()) {
            player.sendMessage(Component.translatable(
                            "rune.feedback.account_level_up",
                            "Account level up! You are now Account Level %s.",
                            Component.text(gain.accountLevelAfter()))
                    .color(NamedTextColor.AQUA));
        }
    }

    /** One decimal at most, e.g. 12.5 or 40. */
    static String format(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}
