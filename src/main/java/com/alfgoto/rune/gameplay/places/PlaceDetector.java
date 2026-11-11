package com.alfgoto.rune.gameplay.places;

import com.alfgoto.rune.foundations.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Answers "which Places is this player in?". The answer is cached per player and only
 * recomputed when the player changes block.
 */
public final class PlaceDetector {

    private record Cached(UUID world, int x, int y, int z, List<Place> places) {
        boolean sameBlock(Location l) {
            return world.equals(l.getWorld().getUID())
                    && x == l.getBlockX()
                    && y == l.getBlockY()
                    && z == l.getBlockZ();
        }
    }

    private final Registry<Place> places;
    private final WorldProbe probe;
    private final Map<UUID, Cached> cache = new HashMap<>();

    public PlaceDetector(Registry<Place> places, WorldProbe probe) {
        this.places = places;
        this.probe = probe;
    }

    /** The Places the player is in right now, in registry order. */
    public List<Place> placesOf(Player player) {
        Location at = player.getLocation();
        Cached cached = cache.get(player.getUniqueId());
        if (cached != null && cached.sameBlock(at)) {
            return cached.places();
        }
        List<Place> result = compute(at);
        cache.put(
                player.getUniqueId(),
                new Cached(at.getWorld().getUID(), at.getBlockX(), at.getBlockY(), at.getBlockZ(), result));
        return result;
    }

    /** Drops the cache entry of a player (on quit). */
    public void forget(UUID player) {
        cache.remove(player);
    }

    private List<Place> compute(Location at) {
        String biome = probe.biomeAt(at);
        Set<String> structures = probe.structuresAt(at);
        List<Place> found = new ArrayList<>();
        for (Place place : places.all()) {
            boolean inside = place.kind() == Place.Kind.BIOME
                    ? place.keys().contains(biome)
                    : structures.stream().anyMatch(place.keys()::contains);
            if (inside) {
                found.add(place);
            }
        }
        return List.copyOf(found);
    }
}
