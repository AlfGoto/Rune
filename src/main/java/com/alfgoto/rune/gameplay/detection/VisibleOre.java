package com.alfgoto.rune.gameplay.detection;

import com.alfgoto.rune.gameplay.runes.StoneFamily;
import java.util.Optional;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

/** Finds the ore a player is looking at (Ore Dash). One ray trace, no scan. */
public final class VisibleOre {

    private VisibleOre() {}

    /** The ore the player looks at within {@code range} blocks of the eyes. Empty when they look at anything else. */
    public static Optional<Block> lookedAt(Player player, double range) {
        Location eye = player.getEyeLocation();
        RayTraceResult hit =
                player.getWorld().rayTraceBlocks(eye, eye.getDirection(), range, FluidCollisionMode.NEVER, true);
        if (hit == null || hit.getHitBlock() == null || !StoneFamily.isOre(hit.getHitBlock().getType())) {
            return Optional.empty();
        }
        return Optional.of(hit.getHitBlock());
    }
}
