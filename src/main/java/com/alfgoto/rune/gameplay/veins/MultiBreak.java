package com.alfgoto.rune.gameplay.veins;

import com.alfgoto.rune.foundations.triggers.Triggers;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The shared multi-block break of docs/jobs/mining.md (Veinminer, later Tunneler, Blast Mine, Treefeller...). Each
 * block is broken as the player, with the tool in hand: it fires the normal break events, so every block gives
 * full Job XP, a Cache roll, drops (Fortune, Smelt...) and tool wear. The breaks are tagged
 * {@link com.alfgoto.rune.foundations.triggers.BreakCause#EFFECT}, so a multi-block effect never starts another.
 */
public final class MultiBreak {

    private final Triggers triggers;

    private final BiPredicate<Player, Block> breaker;

    public MultiBreak(Triggers triggers) {
        this(triggers, Player::breakBlock);
    }

    /** {@code breaker} breaks one block as the player and says whether it did. Tests give their own. */
    public MultiBreak(Triggers triggers, BiPredicate<Player, Block> breaker) {
        this.triggers = triggers;
        this.breaker = breaker;
    }

    /**
     * Breaks {@code blocks} one by one for {@code player}. Stops when the main-hand item is no longer {@code usable}
     * (the tool broke or was swapped).
     */
    public void breakAll(Player player, List<Block> blocks, Predicate<ItemStack> usable) {
        triggers.asEffect(() -> {
            for (Block block : blocks) {
                if (!usable.test(player.getInventory().getItemInMainHand())) {
                    return;
                }
                if (!block.getType().isAir()) {
                    breaker.test(player, block);
                }
            }
        });
    }
}
