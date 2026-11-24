package com.alfgoto.rune.gameplay.blueprints;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Right-click (in the air or on a block) with a Blueprint in the main hand uses it: one Blueprint is taken and its
 * recipe is unlocked. A Blueprint the player already used is kept, with a message.
 */
final class BlueprintListener implements Listener {

    private final Blueprints blueprints;

    BlueprintListener(Blueprints blueprints) {
        this.blueprints = blueprints;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (blueprints.idOf(event.getItem()).isEmpty()) {
            return;
        }
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        Blueprint blueprint = blueprints.of(event.getItem()).orElse(null);
        if (blueprint == null) {
            return;
        }
        Player player = event.getPlayer();
        Component name = Component.translatable(blueprint.translationKey(), blueprint.fallbackName());
        if (!blueprints.unlock(player, blueprint)) {
            player.sendMessage(Component.translatable(
                            "rune.blueprint.already_known", "You already know the recipe: %s.", name)
                    .color(NamedTextColor.RED));
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack hand = inventory.getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);
        inventory.setItemInMainHand(hand.getAmount() <= 0 ? null : hand);
        player.sendMessage(Component.translatable("rune.blueprint.learned", "Recipe unlocked: %s.", name)
                .color(NamedTextColor.GOLD));
    }
}
