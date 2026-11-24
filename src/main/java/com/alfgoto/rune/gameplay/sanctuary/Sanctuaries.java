package com.alfgoto.rune.gameplay.sanctuary;

import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sanctuary: &lt;biome&gt; Runes (docs/jobs/woodcutting.md). Content registers, for each Sanctuary effect id, the
 * Place it works in. The value of a Rune is its Rank (base 1, +1 per Rank), and the values of every Sanctuary
 * that applies add up (Stacking). The total picks the potion effects: Regeneration I, then also Resistance I
 * from {@code resistanceFrom}, then Regeneration II from {@code regeneration2From}.
 */
public final class Sanctuaries {

    /** What a total value gives. {@code regeneration} is the amplifier (0 = I, 1 = II), -1 = none. */
    public record Levels(int regeneration, boolean resistance) {
        public static final Levels NONE = new Levels(-1, false);
    }

    private final double resistanceFrom;
    private final double regeneration2From;
    private final Map<String, String> placeByEffect = new LinkedHashMap<>();

    public Sanctuaries(double resistanceFrom, double regeneration2From) {
        this.resistanceFrom = resistanceFrom;
        this.regeneration2From = regeneration2From;
    }

    /** Declares {@code effectId} as a Sanctuary that works in the Place {@code placeId}. */
    public void register(String effectId, String placeId) {
        if (placeByEffect.putIfAbsent(effectId, placeId) != null) {
            throw new IllegalArgumentException("Sanctuary already registered: " + effectId);
        }
    }

    /** The stacked value of every Sanctuary whose Place is in {@code placeIds}. */
    public double total(List<Stacking.Source> sources, Set<String> placeIds) {
        double total = 0;
        for (Map.Entry<String, String> entry : placeByEffect.entrySet()) {
            if (placeIds.contains(entry.getValue())) {
                total += Stacking.total(entry.getKey(), sources);
            }
        }
        return total;
    }

    /** The potion effects a total value gives. */
    public Levels levels(double total) {
        if (total <= 0) {
            return Levels.NONE;
        }
        int regeneration = total >= regeneration2From ? 1 : 0;
        return new Levels(regeneration, total >= resistanceFrom);
    }
}
