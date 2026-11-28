package com.alfgoto.rune.visuals.unlocktree;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

/**
 * Loads advancements at runtime with Paper's {@code UnsafeValues.loadAdvancement} (deprecated API, but the
 * simplest option: no files, no datapack reload, no dependency).
 */
@SuppressWarnings("deprecation")
final class PaperAdvancementBackend implements AdvancementBackend {

    @Override
    public void load(NamespacedKey key, String json) {
        if (Bukkit.getAdvancement(key) != null) {
            Bukkit.getUnsafe().removeAdvancement(key);
        }
        Bukkit.getUnsafe().loadAdvancement(key, json);
    }

    @Override
    public boolean isDone(Player player, NamespacedKey key) {
        return progress(player, key).isDone();
    }

    @Override
    public void award(Player player, NamespacedKey key) {
        progress(player, key).awardCriteria(TreeJson.CRITERION);
    }

    @Override
    public void revoke(Player player, NamespacedKey key) {
        progress(player, key).revokeCriteria(TreeJson.CRITERION);
    }

    private static AdvancementProgress progress(Player player, NamespacedKey key) {
        Advancement advancement = Bukkit.getAdvancement(key);
        if (advancement == null) {
            throw new IllegalStateException("Advancement " + key + " is not loaded");
        }
        return player.getAdvancementProgress(advancement);
    }
}
