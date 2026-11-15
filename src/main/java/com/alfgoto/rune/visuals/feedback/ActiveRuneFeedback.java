package com.alfgoto.rune.visuals.feedback;

import com.alfgoto.rune.gameplay.runes.ActiveRuneListener;
import com.alfgoto.rune.gameplay.runes.Rune;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** Shows Active Rune use and cooldowns in the action bar. */
public final class ActiveRuneFeedback implements ActiveRuneListener {

    @Override
    public void onUsed(Player player, Rune rune, double seconds) {
        if (seconds <= 0) {
            return;
        }
        player.sendActionBar(Component.translatable(
                        "rune.feedback.active_used",
                        "%s used. Cooldown: %ss",
                        name(rune),
                        Component.text(XpFeedback.format(seconds)))
                .color(NamedTextColor.GREEN));
    }

    @Override
    public void onCooldown(Player player, Rune rune, double secondsLeft) {
        player.sendActionBar(Component.translatable(
                        "rune.feedback.active_cooldown",
                        "%s is recharging: %ss",
                        name(rune),
                        Component.text(XpFeedback.format(Math.ceil(secondsLeft * 10) / 10)))
                .color(NamedTextColor.RED));
    }

    private static Component name(Rune rune) {
        return Component.translatable(rune.item().translationKey(), rune.item().fallbackName());
    }
}
