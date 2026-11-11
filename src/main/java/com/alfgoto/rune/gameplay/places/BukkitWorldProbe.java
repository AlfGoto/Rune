package com.alfgoto.rune.gameplay.places;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.StructurePiece;

/** {@link WorldProbe} backed by the real world. */
public final class BukkitWorldProbe implements WorldProbe {

    @Override
    public String biomeAt(Location location) {
        return location.getBlock().getBiome().getKey().toString();
    }

    @Override
    public Set<String> structuresAt(Location location) {
        Set<String> found = new HashSet<>();
        World world = location.getWorld();
        double x = location.getBlockX() + 0.5;
        double y = location.getBlockY() + 0.5;
        double z = location.getBlockZ() + 0.5;
        // Pieces, not the whole structure box: a Mineshaft box is huge and mostly solid rock.
        for (GeneratedStructure structure : world.getStructures(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            for (StructurePiece piece : structure.getPieces()) {
                if (piece.getBoundingBox().contains(x, y, z)) {
                    NamespacedKey key = Registry.STRUCTURE.getKey(structure.getStructure());
                    if (key != null) {
                        found.add(key.toString());
                    }
                    break;
                }
            }
        }
        return found;
    }
}
