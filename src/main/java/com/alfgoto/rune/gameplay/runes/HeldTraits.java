package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.gameplay.effects.Stacking;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Effects that work while a Rune Tool is held in the main hand (Trait bindings or installed Runes), checked
 * every couple of ticks:
 * <ul>
 *   <li>{@value #GLOW}: an invisible Light block at the player's head. The value is the light level (0 to 15).
 *       It is sent to nearby players only (never placed in the world), so nothing is left behind.</li>
 *   <li>{@value #NO_WATER_PENALTY}: cancels the vanilla underwater mining penalty.</li>
 *   <li>{@value #NO_AIR_PENALTY}: cancels the vanilla "not on the ground" mining penalty.</li>
 *   <li>Move speed effects: content registers an effect id and the rule that says where it applies; the value
 *       is a % move speed.</li>
 * </ul>
 */
public final class HeldTraits implements Runnable, Listener {

    public static final String GLOW = "glow";
    public static final String NO_WATER_PENALTY = "no_water_penalty";
    public static final String NO_AIR_PENALTY = "no_air_penalty";

    /** Vanilla slows mining by 5 in water and in the air; x5 cancels one penalty. */
    private static final double PENALTY_FACTOR = 4;

    /** Vanilla underwater mining speed attribute is 0.2; +0.8 brings it back to 1. */
    private static final double WATER_PENALTY_FIX = 0.8;

    /** The Light block is sent again every this many checks, in case a chunk update erased it. */
    private static final int RESEND_EVERY = 10;

    private static final double VIEW_DISTANCE = 64;

    private static final class Glow {
        final Location location;
        final int level;
        int age;

        Glow(Location location, int level) {
            this.location = location;
            this.level = level;
        }
    }

    private final ToolLoadout loadout;
    private final Predicate<ItemStack> isBroken;
    private final Plugin plugin;
    private final NamespacedKey moveKey;
    private final NamespacedKey waterKey;
    private final NamespacedKey airKey;
    private final Map<String, Predicate<Player>> moveRules = new LinkedHashMap<>();
    private final Map<UUID, Glow> glows = new HashMap<>();

    public HeldTraits(Plugin plugin, ToolLoadout loadout, Predicate<ItemStack> isBroken) {
        this.plugin = plugin;
        this.loadout = loadout;
        this.isBroken = isBroken;
        this.moveKey = new NamespacedKey(plugin, "held_move_speed");
        this.waterKey = new NamespacedKey(plugin, "held_no_water_penalty");
        this.airKey = new NamespacedKey(plugin, "held_no_air_penalty");
    }

    /** Declares {@code effectId} as a % move speed that applies while {@code applies} is true. */
    public void registerMoveSpeed(String effectId, Predicate<Player> applies) {
        if (moveRules.putIfAbsent(effectId, applies) != null) {
            throw new IllegalArgumentException("Move speed effect already registered: " + effectId);
        }
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            List<Stacking.Source> sources =
                    hand.getType().isAir() || isBroken.test(hand) ? List.of() : loadout.sources(hand, player);
            glow(player, (int) Math.min(15, Math.max(0, Stacking.total(GLOW, sources))));
            moveSpeed(player, sources);
            penalties(player, sources);
        }
    }

    /** Removes everything this class put on players and in their view (on shutdown). */
    public void clear() {
        for (Glow glow : glows.values()) {
            restore(glow);
        }
        glows.clear();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            set(player.getAttribute(Attribute.MOVEMENT_SPEED), moveKey, 0, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            set(player.getAttribute(Attribute.SUBMERGED_MINING_SPEED), waterKey, 0, AttributeModifier.Operation.ADD_NUMBER);
            set(player.getAttribute(Attribute.BLOCK_BREAK_SPEED), airKey, 0, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Glow glow = glows.remove(event.getPlayer().getUniqueId());
        if (glow != null) {
            restore(glow);
        }
    }

    private void penalties(Player player, List<Stacking.Source> sources) {
        boolean noWater = Stacking.total(NO_WATER_PENALTY, sources) > 0;
        boolean noAir = Stacking.total(NO_AIR_PENALTY, sources) > 0 && !player.isOnGround();
        set(player.getAttribute(Attribute.SUBMERGED_MINING_SPEED), waterKey, noWater ? WATER_PENALTY_FIX : 0,
                AttributeModifier.Operation.ADD_NUMBER);
        set(player.getAttribute(Attribute.BLOCK_BREAK_SPEED), airKey, noAir ? PENALTY_FACTOR : 0,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    private void moveSpeed(Player player, List<Stacking.Source> sources) {
        double percent = 0;
        for (Map.Entry<String, Predicate<Player>> rule : moveRules.entrySet()) {
            if (rule.getValue().test(player)) {
                percent += Stacking.total(rule.getKey(), sources);
            }
        }
        set(player.getAttribute(Attribute.MOVEMENT_SPEED), moveKey, percent / 100,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    }

    /** Sets (or, with 0, removes) a transient modifier. Does nothing when it already has this amount. */
    private static void set(AttributeInstance attribute, NamespacedKey key, double amount,
            AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }
        AttributeModifier current = attribute.getModifier(key);
        if (current != null && current.getAmount() == amount) {
            return;
        }
        if (current != null) {
            attribute.removeModifier(key);
        }
        if (amount != 0) {
            attribute.addTransientModifier(new AttributeModifier(key, amount, operation));
        }
    }

    private void glow(Player player, int level) {
        UUID id = player.getUniqueId();
        Glow old = glows.get(id);
        Block head = level > 0 ? player.getEyeLocation().getBlock() : null;
        if (head != null && !head.getType().isAir()) {
            head = null; // the head is inside a block or liquid: no room for a Light block
        }
        if (head == null) {
            if (old != null) {
                restore(old);
                glows.remove(id);
            }
            return;
        }
        Location at = head.getLocation();
        if (old != null) {
            if (old.location.equals(at) && old.level == level) {
                if (++old.age % RESEND_EVERY != 0) {
                    return;
                }
                show(old);
                return;
            }
            restore(old);
        }
        Glow glow = new Glow(at, level);
        glows.put(id, glow);
        show(glow);
    }

    private void show(Glow glow) {
        BlockData light = Bukkit.createBlockData("minecraft:light[level=" + glow.level + "]");
        for (Player viewer : glow.location.getWorld().getPlayers()) {
            if (viewer.getLocation().distanceSquared(glow.location) <= VIEW_DISTANCE * VIEW_DISTANCE) {
                viewer.sendBlockChange(glow.location, light);
            }
        }
    }

    private void restore(Glow glow) {
        BlockData real = glow.location.getBlock().getBlockData();
        for (Player viewer : glow.location.getWorld().getPlayers()) {
            viewer.sendBlockChange(glow.location, real);
        }
    }
}
