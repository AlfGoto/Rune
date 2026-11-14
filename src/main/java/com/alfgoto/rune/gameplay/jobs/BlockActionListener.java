package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import java.util.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Turns a player's block break into Job XP: looks the block up in {@link BlockActions}, applies
 * {@link BlockXpRules}, and grants the result through {@link JobProgress}.
 *
 * <p>Block Tracking clears a block's tags when it breaks, so this must run before that.
 * {@code BlockTrackingModule} depends on {@code TriggersModule} to keep that order.
 */
public final class BlockActionListener implements Listener {

    private final BlockActions actions;
    private final BlockXpRules rules;
    private final BlockTracker tracker;
    private final JobProgress progress;

    public BlockActionListener(
            BlockActions actions, BlockXpRules rules, BlockTracker tracker, JobProgress progress) {
        this.actions = actions;
        this.rules = rules;
        this.tracker = tracker;
        this.progress = progress;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(RuneBlockBreakEvent event) {
        Optional<BlockAction> action = actions.get(event.block().getType());
        if (action.isEmpty()) {
            return;
        }
        double multiplier = rules.multiplier(event.block().getType(), tracker.tags(event.block()));
        double xp = action.get().xp() * multiplier;
        if (xp > 0) {
            progress.addJobXp(event.player().getUniqueId(), action.get().job(), xp);
        }
    }
}
