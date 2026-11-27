package com.alfgoto.rune.visuals.feedback;

import com.alfgoto.rune.gameplay.woodcutting.RhythmChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Shows the Rhythm charges in the action bar, only to players who wear a piece with a Rhythm Trait. */
public final class RhythmFeedback implements Listener {

    @EventHandler
    public void onRhythmChange(RhythmChangeEvent event) {
        if (!event.wearingRhythmGear()) {
            return;
        }
        if (event.charges() == 0) {
            event.player()
                    .sendActionBar(Component.translatable("rune.feedback.rhythm_lost", "Rhythm lost")
                            .color(NamedTextColor.GRAY));
            return;
        }
        event.player()
                .sendActionBar(Component.translatable(
                                "rune.feedback.rhythm",
                                "Rhythm: %s/%s",
                                Component.text(event.charges()),
                                Component.text(event.maxCharges()))
                        .color(NamedTextColor.GREEN));
    }
}
