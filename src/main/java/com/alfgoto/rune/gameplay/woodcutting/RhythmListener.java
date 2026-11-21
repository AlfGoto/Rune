package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.foundations.triggers.TreeFelled;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Adds a Rhythm charge for each Felled Tree and clears them after the reset time. Both changes fire a
 * {@link RhythmChangeEvent}. Charges are counted for everyone; the Traits only read them on worn Rhythm gear.
 */
final class RhythmListener implements Listener {

    private final Plugin plugin;
    private final Rhythm rhythm;
    private final ToolLoadout loadout;
    private final long resetTicks;
    private final Map<UUID, BukkitTask> resets = new HashMap<>();

    RhythmListener(Plugin plugin, Rhythm rhythm, ToolLoadout loadout, int resetSeconds) {
        this.plugin = plugin;
        this.rhythm = rhythm;
        this.loadout = loadout;
        this.resetTicks = resetSeconds * 20L;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFelled(TreeFelled event) {
        Player player = event.player();
        UUID id = player.getUniqueId();
        int charges = rhythm.add(id);
        BukkitTask old = resets.remove(id);
        if (old != null) {
            old.cancel();
        }
        resets.put(id, plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            resets.remove(id);
            rhythm.reset(id);
            if (player.isOnline()) {
                fire(player, 0);
            }
        }, resetTicks));
        fire(player, charges);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        BukkitTask task = resets.remove(id);
        if (task != null) {
            task.cancel();
        }
        rhythm.reset(id);
    }

    private void fire(Player player, int charges) {
        boolean worn = loadout.wornSources(player).stream()
                .map(Stacking.Source::effectId)
                .anyMatch(RhythmRules::isRhythmEffect);
        plugin.getServer()
                .getPluginManager()
                .callEvent(new RhythmChangeEvent(player, charges, rhythm.maxCharges(), worn));
    }
}
