package com.alfgoto.rune.gameplay.traits;

import com.alfgoto.rune.foundations.config.TraitConfig;
import com.alfgoto.rune.gameplay.effects.Scale;
import com.alfgoto.rune.gameplay.items.RuneItem;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * The Momentum Trait (docs/jobs/mining.md, woodcutting.md): the more blocks a player breaks in a row, the faster
 * the tool. Each break within {@code tools.momentum.reset-seconds} of the last one adds one block, up to
 * {@code tools.momentum.max-blocks}; a longer pause resets it. The streak is per player and kept in memory
 * (it resets on restart or when the player leaves).
 *
 * <p>The Scale {@code momentum} is blocks in a row / max blocks. The speed itself is a Trait binding on the
 * {@code haste} effect, so it stacks with Haste Runes.
 */
public final class Momentum {

    /** The id of the Scale: blocks in a row / max blocks. */
    public static final String SCALE_ID = "momentum";

    private record Streak(int blocks, long lastBreak) {}

    private final TraitConfig config;
    private final LongSupplier clock;
    private final Map<UUID, Streak> streaks = new HashMap<>();

    public Momentum(TraitConfig config) {
        this(config, System::currentTimeMillis);
    }

    /** {@code clock} gives the time in milliseconds. */
    public Momentum(TraitConfig config, LongSupplier clock) {
        this.config = config;
        this.clock = clock;
    }

    /** The Scale {@code momentum}: blocks in a row / max blocks, for the player (0 with no player). */
    public Scale scale() {
        return new Scale() {
            @Override
            public String id() {
                return SCALE_ID;
            }

            @Override
            public double at(@Nullable Player player, @Nullable ItemStack stack) {
                return player == null ? 0 : fraction(player.getUniqueId());
            }
        };
    }

    /** The player broke a block: one more block in a row, or a new streak of 1 after a pause. */
    public void hit(UUID player) {
        long now = clock.getAsLong();
        int blocks = blocks(player) + 1;
        streaks.put(player, new Streak(Math.min(blocks, config.momentumMaxBlocks()), now));
    }

    /** Blocks in a row right now (0 once the pause is over). */
    public int blocks(UUID player) {
        Streak streak = streaks.get(player);
        if (streak == null) {
            return 0;
        }
        if (clock.getAsLong() - streak.lastBreak() > config.momentumResetSeconds() * 1000) {
            streaks.remove(player);
            return 0;
        }
        return streak.blocks();
    }

    /** Blocks in a row / max blocks, from 0 to 1. */
    public double fraction(UUID player) {
        return blocks(player) / (double) config.momentumMaxBlocks();
    }

    /** Forgets the streak of a player (they left). */
    public void forget(UUID player) {
        streaks.remove(player);
    }
}
