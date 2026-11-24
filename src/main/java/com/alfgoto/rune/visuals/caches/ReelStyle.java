package com.alfgoto.rune.visuals.caches;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Sound;

/**
 * Colours, weights and sounds of the Cache reel ("Trial Vault" sound theme, vanilla sounds only).
 * All of this is visual, so it stays in code and not in the config.
 */
final class ReelStyle {

    /** Weight of each Rarity for the filler items, in {@link Rarity} order: Common to Legendary. */
    private static final int[] WEIGHTS = {50, 25, 15, 7, 3};

    /** The last moves of the reel that use the heartbeat drum instead of the hat. */
    static final int DRUM_MOVES = 4;

    private ReelStyle() {}

    /** One sound, played {@code delay} ticks after the cue starts. */
    record Cue(int delay, Sound sound, float volume, float pitch) {}

    /** The Rarity of a filler item: Common 50, Uncommon 25, Rare 15, Epic 7, Legendary 3. */
    static Rarity weighted(RandomGenerator random) {
        int total = 0;
        for (int w : WEIGHTS) {
            total += w;
        }
        int roll = random.nextInt(total);
        Rarity[] all = Rarity.values();
        for (int i = 0; i < all.length; i++) {
            roll -= WEIGHTS[i];
            if (roll < 0) {
                return all[i];
            }
        }
        return all[all.length - 1];
    }

    /** The best Rarity among the Drops. Common when none has a Rarity. */
    static Rarity landed(List<Drop> drops) {
        Rarity best = Rarity.COMMON;
        for (Drop drop : drops) {
            Rarity rarity = drop.rarity();
            if (rarity != null && rarity.ordinal() > best.ordinal()) {
                best = rarity;
            }
        }
        return best;
    }

    /** When the reel opens. */
    static List<Cue> open() {
        return List.of(
                new Cue(0, Sound.BLOCK_VAULT_ACTIVATE, 1f, 1f), new Cue(0, Sound.BLOCK_VAULT_OPEN_SHUTTER, 1f, 1f));
    }

    /** One move of the reel. The last {@link #DRUM_MOVES} moves are a drum, like a heartbeat. */
    static Cue tick(int step, int steps) {
        float pitch = 1.0f + step * 0.03f;
        if (step > steps - DRUM_MOVES) {
            return new Cue(0, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.8f, pitch);
        }
        return new Cue(0, Sound.BLOCK_NOTE_BLOCK_HAT, 0.4f, pitch);
    }

    /** The reel lands on its last pane. */
    static Cue land() {
        return new Cue(0, Sound.BLOCK_VAULT_EJECT_ITEM, 1f, 1f);
    }

    /** The reveal, by landed Rarity. Legendary staggers its firework sounds. */
    static List<Cue> reveal(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> List.of(new Cue(0, Sound.ENTITY_ITEM_PICKUP, 1f, 1f));
            case UNCOMMON -> List.of(new Cue(0, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f));
            case RARE -> List.of(
                    new Cue(0, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f),
                    new Cue(0, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f));
            case EPIC -> List.of(
                    new Cue(0, Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f),
                    new Cue(0, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f));
            case LEGENDARY -> List.of(
                    new Cue(0, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f),
                    new Cue(2, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f),
                    new Cue(5, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f),
                    new Cue(8, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1f));
        };
    }
}
