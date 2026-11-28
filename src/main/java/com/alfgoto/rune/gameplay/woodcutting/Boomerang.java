package com.alfgoto.rune.gameplay.woodcutting;

import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Boomerang: the axe flies along the player's look, breaks the first log it meets, and comes back. */
public final class Boomerang {

    /** Effect id of Boomerang: the range in blocks. */
    public static final String BOOMERANG = "boomerang";

    private static final double STEP = 0.5;
    /** Blocks the flying axe travels each tick. */
    private static final double SPEED = 1.5;
    /** The axe is caught when it is this close to the player. */
    private static final double CATCH_DISTANCE = 1.5;
    private static final float SPIN_DEGREES = 50f;

    private Boomerang() {}

    /**
     * The first log on the player's look line within {@code range} blocks. The axe flies through leaves and open
     * air and stops on any other solid block: then there is no log to break.
     */
    public static Optional<Block> firstLog(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        for (double distance = STEP; distance <= range; distance += STEP) {
            Block block = eye.clone().add(direction.clone().multiply(distance)).getBlock();
            if (Tag.LOGS.isTagged(block.getType())) {
                return Optional.of(block);
            }
            if (!block.isPassable() && !Tag.LEAVES.isTagged(block.getType())) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /** Shows the axe flying to {@code target} and back to the player. Display only: the log is broken elsewhere. */
    public static void fly(Plugin plugin, Player player, ItemStack axe, Block target) {
        Location start = player.getEyeLocation().subtract(0, 0.3, 0);
        ItemStack shown = axe.clone();
        shown.setAmount(1);
        ItemDisplay display = (ItemDisplay) player.getWorld().spawnEntity(start, EntityType.ITEM_DISPLAY);
        display.setItemStack(shown);
        display.setBillboard(Display.Billboard.FIXED);
        display.setTeleportDuration(1);
        Location goal = target.getLocation().add(0.5, 0.5, 0.5);
        player.getWorld().playSound(start, Sound.ITEM_TRIDENT_THROW, 1f, 1f);
        new BukkitRunnable() {
            private boolean returning;
            private int ticks;

            @Override
            public void run() {
                ticks++;
                if (!display.isValid() || !player.isOnline() || ticks > 200) {
                    display.remove();
                    cancel();
                    return;
                }
                Location at = display.getLocation();
                Location aim = returning ? player.getEyeLocation().subtract(0, 0.3, 0) : goal;
                Vector to = aim.toVector().subtract(at.toVector());
                double left = to.length();
                if (left <= (returning ? CATCH_DISTANCE : SPEED)) {
                    if (returning) {
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
                        display.remove();
                        cancel();
                        return;
                    }
                    returning = true;
                    return;
                }
                Location next = at.add(to.normalize().multiply(SPEED));
                next.setYaw(next.getYaw() + SPIN_DEGREES);
                display.teleport(next);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
