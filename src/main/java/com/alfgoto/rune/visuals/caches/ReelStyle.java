package com.alfgoto.rune.visuals.caches;

import com.alfgoto.rune.text.Rarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/** Colours and sounds of the Cache reel, per Rarity. Rough for now. */
final class ReelStyle {

    private ReelStyle() {}

    static Material pane(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> Material.WHITE_STAINED_GLASS_PANE;
            case UNCOMMON -> Material.LIME_STAINED_GLASS_PANE;
            case RARE -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            case EPIC -> Material.MAGENTA_STAINED_GLASS_PANE;
            case LEGENDARY -> Material.ORANGE_STAINED_GLASS_PANE;
        };
    }

    static Component name(Rarity rarity) {
        return Component.translatable(rarity.translationKey(), rarity.englishName())
                .color(rarity.color());
    }

    /** Legendary plays a firework; the other Rarities a chime that gets higher with the Rarity. */
    static void playRevealSound(Player player, Rarity rarity) {
        var at = player.getLocation();
        if (rarity == Rarity.LEGENDARY) {
            player.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1f, 1f);
            player.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 1f, 1f);
            player.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.MASTER, 1f, 1f);
        } else {
            player.playSound(
                    at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1f, 0.8f + rarity.ordinal() * 0.2f);
        }
    }
}
