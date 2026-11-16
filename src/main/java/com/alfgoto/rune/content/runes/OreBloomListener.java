package com.alfgoto.rune.content.runes;

import com.alfgoto.rune.foundations.blocktracking.BlockTracker;
import com.alfgoto.rune.foundations.triggers.RuneBlockBreakEvent;
import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.StoneFamily;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Ore Bloom (docs/jobs/mining.md): when the player mines an ore, a neighbouring stone-family block can become
 * that ore. Each full 100% of the stacked chance is a guaranteed bloom and the rest is the chance of one more
 * (Stacking). Silk Touch turns it off through {@link Stacking#total}.
 *
 * <p>A bloomed ore is a plain natural ore: it is not tagged in Block Tracking, so it gives full Job XP, and it
 * can bloom again when mined. Neighbours that Block Tracking remembers (placed by a player, made by fluids,
 * grown) are left alone, so placing stone around cannot farm ores.
 */
final class OreBloomListener implements Listener {

    static final String EFFECT_ID = "ore_bloom";

    private static final BlockFace[] FACES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final ItemRegistry items;
    private final ToolLoadout loadout;
    private final BlockTracker tracker;
    private final DoubleSupplier random;

    OreBloomListener(ItemRegistry items, ToolLoadout loadout, BlockTracker tracker, DoubleSupplier random) {
        this.items = items;
        this.loadout = loadout;
        this.tracker = tracker;
        this.random = random;
    }

    @EventHandler
    public void onBlockBreak(RuneBlockBreakEvent event) {
        // The break event fires before the block is removed, so the block is still the ore.
        Material ore = event.block().getType();
        if (!StoneFamily.isOre(ore) || items.of(event.item()).isEmpty()) {
            return;
        }
        int blooms = Stacking.rolls(Stacking.total(EFFECT_ID, loadout.sources(event.item())), random);
        for (int i = 0; i < blooms; i++) {
            if (!bloomOnce(event.block(), ore)) {
                return; // no neighbour left
            }
        }
    }

    /** Picks one random neighbour that can take the ore and turns it into the ore. False if none can. */
    private boolean bloomOnce(Block origin, Material ore) {
        List<Block> candidates = new ArrayList<>();
        for (BlockFace face : FACES) {
            Block neighbour = origin.getRelative(face);
            if (StoneFamily.bloomInto(ore, neighbour.getType()).isPresent()
                    && tracker.tags(neighbour).isEmpty()) {
                candidates.add(neighbour);
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        Block target = candidates.get(Math.min(candidates.size() - 1, (int) (random.getAsDouble() * candidates.size())));
        target.setType(StoneFamily.bloomInto(ore, target.getType()).orElseThrow());
        target.getWorld()
                .spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation().add(0.5, 0.5, 0.5), 8, 0.3, 0.3, 0.3, 0);
        return true;
    }
}
