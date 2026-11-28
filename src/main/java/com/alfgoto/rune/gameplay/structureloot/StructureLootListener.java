package com.alfgoto.rune.gameplay.structureloot;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Adds the structure loot to a chest when it is generated (first opened). The chest is generated for the player
 * who opens it, so the Job Level gate is theirs. A chest with no player (a dispenser-like source) gets nothing.
 */
final class StructureLootListener implements Listener {

    private final StructureLoot structureLoot;

    StructureLootListener(StructureLoot structureLoot) {
        this.structureLoot = structureLoot;
    }

    @EventHandler
    public void onLoot(LootGenerateEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        String table = event.getLootTable().getKey().toString();
        List<ItemStack> loot = event.getLoot();
        for (StructureLoot.Result result : structureLoot.roll(player.getUniqueId(), table, ThreadLocalRandom.current())) {
            loot.add(result.stack());
        }
    }
}
