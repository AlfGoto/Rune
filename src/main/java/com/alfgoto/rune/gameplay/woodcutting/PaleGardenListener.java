package com.alfgoto.rune.gameplay.woodcutting;

import com.alfgoto.rune.gameplay.places.PlaceDetector;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Creaking;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Pale Oak Axe: while it is in the main hand and the player is in the Pale Garden, the player has night
 * vision and Creakings lose track of them. (The daylight slowdown is a mining speed rule, see
 * {@code EssenceAxeRulesModule}.)
 */
final class PaleGardenListener implements Listener {

    static final String PLACE = "pale_garden";

    /** Creakings this close to the player are told to forget them. */
    private static final double CREAKING_REACH = 32;

    private final Plugin plugin;
    private final HeldAxe held;
    private final PlaceDetector places;
    /** Night vision length (from config). It is renewed every 10 ticks, so it never flickers. */
    private final int nightVisionTicks;
    private final Set<UUID> withNightVision = new HashSet<>();

    PaleGardenListener(Plugin plugin, HeldAxe held, PlaceDetector places, int nightVisionTicks) {
        this.nightVisionTicks = nightVisionTicks;
        this.plugin = plugin;
        this.held = held;
        this.places = places;
    }

    /** Starts the repeating check (every 10 ticks). */
    void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 10L, 10L);
    }

    private void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            boolean inGarden = places.isIn(player, PLACE);
            nightVision(player, inGarden && held.value(player, AxeTraits.PALE_NIGHT_VISION) > 0);
            if (inGarden && held.value(player, AxeTraits.CREAKING_LOSE_TRACK) > 0) {
                for (Entity near : player.getNearbyEntities(CREAKING_REACH, CREAKING_REACH, CREAKING_REACH)) {
                    if (near instanceof Creaking creaking && player.equals(creaking.getTarget())) {
                        creaking.setTarget(null);
                    }
                }
            }
        }
    }

    private void nightVision(Player player, boolean on) {
        UUID id = player.getUniqueId();
        if (on) {
            PotionEffect current = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
            // Never replace a longer night vision (potion, beacon).
            if (current == null || current.getDuration() <= nightVisionTicks) {
                player.addPotionEffect(
                        new PotionEffect(PotionEffectType.NIGHT_VISION, nightVisionTicks, 0, true, false, true));
                withNightVision.add(id);
            }
        } else if (withNightVision.remove(id)) {
            PotionEffect current = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
            if (current != null && current.getDuration() <= nightVisionTicks) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        }
    }

    /** A Creaking cannot pick the player as a target at all. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Creaking
                && event.getTarget() instanceof Player player
                && places.isIn(player, PLACE)
                && held.value(player, AxeTraits.CREAKING_LOSE_TRACK) > 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        withNightVision.remove(event.getPlayer().getUniqueId());
    }
}
