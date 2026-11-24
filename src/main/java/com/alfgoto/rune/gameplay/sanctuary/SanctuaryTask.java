package com.alfgoto.rune.gameplay.sanctuary;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.places.Place;
import com.alfgoto.rune.gameplay.places.PlaceDetector;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Every second, gives each online player the Sanctuary potion effects of the Runes on them and in the armor
 * they wear, while they stand in the Place. The effects are short, so they end soon after the player leaves.
 */
final class SanctuaryTask implements Runnable {

    /** How often the task runs, in ticks. */
    static final long PERIOD = 20;

    private final Server server;
    private final Sanctuaries sanctuaries;
    private final PlayerLoadout playerLoadout;
    private final ToolLoadout toolLoadout;
    private final PlaceDetector detector;
    private final int durationTicks;

    SanctuaryTask(
            Server server,
            Sanctuaries sanctuaries,
            PlayerLoadout playerLoadout,
            ToolLoadout toolLoadout,
            PlaceDetector detector,
            int durationTicks) {
        this.server = server;
        this.sanctuaries = sanctuaries;
        this.playerLoadout = playerLoadout;
        this.toolLoadout = toolLoadout;
        this.detector = detector;
        this.durationTicks = durationTicks;
    }

    @Override
    public void run() {
        for (Player player : server.getOnlinePlayers()) {
            apply(player);
        }
    }

    void apply(Player player) {
        Set<String> places =
                detector.placesOf(player).stream().map(Place::id).collect(Collectors.toSet());
        if (places.isEmpty()) {
            return;
        }
        List<Stacking.Source> sources = new ArrayList<>(playerLoadout.sources(player.getUniqueId()));
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece != null && !piece.getType().isAir()) {
                sources.addAll(toolLoadout.sources(piece, player));
            }
        }
        Sanctuaries.Levels levels = sanctuaries.levels(sanctuaries.total(sources, places));
        if (levels.regeneration() >= 0) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, durationTicks, levels.regeneration(), true, true, true));
        }
        if (levels.resistance()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, 0, true, true, true));
        }
    }
}
