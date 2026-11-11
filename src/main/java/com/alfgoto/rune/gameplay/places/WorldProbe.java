package com.alfgoto.rune.gameplay.places;

import java.util.Set;
import org.bukkit.Location;

/** Asks the world what is at a location. Split out so detection can be tested without worldgen. */
public interface WorldProbe {

    /** Key of the biome at the location, e.g. {@code minecraft:taiga}. */
    String biomeAt(Location location);

    /** Keys of the structures whose pieces contain the location, e.g. {@code minecraft:mineshaft}. */
    Set<String> structuresAt(Location location);
}
