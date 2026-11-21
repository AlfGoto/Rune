package com.alfgoto.rune.visuals.caches;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Cache reel: a row of coloured panes that scrolls past a pointer, slows down, and lands on the
 * Cache's Rarity. Then the menu flashes in that colour and plays a sound (firework for Legendary).
 * The loot is given once: when the reel ends, or when the player closes the menu early.
 */
final class ReelMenu extends Menu {

    static final int STEPS = 24;
    private static final int ROW = 9;
    private static final int POINTER = 4;
    private static final int CENTER = ROW + 4;

    private final Plugin plugin;
    private final Player player;
    private final Rarity rarity;
    private final List<Drop> drops;
    private final Runnable finish;
    private final Rarity[] strip = new Rarity[STEPS + ROW];
    private int step;
    private BukkitTask task;
    private boolean done;

    ReelMenu(Plugin plugin, Player player, Rarity rarity, List<Drop> drops, Runnable finish, RandomGenerator random) {
        super(Text.translatable("rune.cache.reel.title", "Opening a Cache").color(rarity.color()), 3);
        this.plugin = plugin;
        this.player = player;
        this.rarity = rarity;
        this.drops = drops;
        this.finish = finish;
        Rarity[] all = Rarity.values();
        for (int i = 0; i < strip.length; i++) {
            strip[i] = all[random.nextInt(all.length)];
        }
        strip[STEPS - 1 + CENTER - ROW] = rarity; // the pane under the pointer on the last step
        set(POINTER, Button.of(Material.HOPPER, Component.empty(), p -> {}));
        draw();
    }

    /** Opens the menu and starts the reel. */
    void start() {
        open(player);
        if (player.getOpenInventory().getTopInventory().getHolder() != this) {
            complete(); // the menu could not open: never lose the loot
            return;
        }
        schedule();
    }

    /** Ticks to wait before the next move: starts fast, slows down. */
    static int delay(int step) {
        return 1 + step * step / 80;
    }

    private void schedule() {
        task = plugin.getServer().getScheduler().runTaskLater(plugin, this::advance, delay(step));
    }

    private void advance() {
        step++;
        draw();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.0f + step * 0.03f);
        if (step < STEPS - 1) {
            schedule();
        } else {
            task = null;
            reveal();
        }
    }

    private void draw() {
        for (int i = 0; i < ROW; i++) {
            set(ROW + i, Button.of(ReelStyle.pane(strip[step + i]), ReelStyle.name(strip[step + i]), p -> {}));
        }
    }

    /** Flash in the Cache's colour, sound, and show the result. Then the loot is given. */
    private void reveal() {
        Component name = ReelStyle.name(rarity);
        for (int slot = 0; slot < size(); slot++) {
            if (slot != CENTER) {
                set(slot, Button.of(ReelStyle.pane(rarity), name, p -> {}));
            }
        }
        ItemStack icon = new ItemStack(Material.NETHER_STAR);
        List<Component> lore = new ArrayList<>();
        for (Drop drop : drops) {
            lore.add(drop.describe().decoration(TextDecoration.ITALIC, false));
        }
        icon.editMeta(meta -> {
            meta.displayName(Text.translatable("rune.cache.reel.result", "Cache opened!")
                    .color(rarity.color())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        set(CENTER, new Button(icon, p -> {}));
        ReelStyle.playRevealSound(player, rarity);
        complete();
    }

    /** Gives the loot (once) and stops the animation. Called at the end and when the menu is closed. */
    void complete() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (!done) {
            done = true;
            finish.run();
        }
    }
}
