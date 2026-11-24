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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Cache reel: a row of coloured panes that scrolls past a pointer, slows down, and lands on the
 * best Rarity among the loot (CS:GO style). Then the menu pulses in that colour, plays a sound and
 * shows each Drop. The loot is given once: at the reveal, or when the player closes the menu early.
 */
final class ReelMenu extends Menu {

    static final int STEPS = 24;
    /** Ticks between the landing and the reveal. */
    static final int LAND_PAUSE = 8;
    /** Ticks the whole menu stays in the landed colour. */
    static final int FLASH = 3;

    private static final int ROW = 9;
    private static final int POINTER = 4;
    private static final int CENTER = ROW + 4;
    private static final int POINTER_BELOW = 2 * ROW + 4;
    /** Corners and sides that take the landed colour once the flash is over. */
    private static final int[] BORDER = {0, 1, 7, 8, 9, 17, 18, 19, 25, 26};

    private final Plugin plugin;
    private final Player player;
    private final Rarity landed;
    private final List<Drop> drops;
    private final Runnable finish;
    private final Rarity[] strip = new Rarity[STEPS + ROW];
    private final List<BukkitTask> tasks = new ArrayList<>();
    private int step;
    private boolean done;

    ReelMenu(Plugin plugin, Player player, List<Drop> drops, Runnable finish, RandomGenerator random) {
        super(Text.translatable("rune.cache.reel.title", "Opening a Cache").color(ReelStyle.landed(drops).color()), 3);
        this.plugin = plugin;
        this.player = player;
        this.drops = drops;
        this.finish = finish;
        this.landed = ReelStyle.landed(drops);
        for (int i = 0; i < strip.length; i++) {
            strip[i] = ReelStyle.weighted(random);
        }
        int landing = STEPS + POINTER; // the pane under the pointer on the last move
        strip[landing] = landed;
        nearMiss(landing, random);
        frame();
        draw();
    }

    /** Puts 1 or 2 better panes next to the landing pane, unless it is already Legendary. */
    private void nearMiss(int landing, RandomGenerator random) {
        if (landed == Rarity.LEGENDARY) {
            return;
        }
        boolean right = random.nextBoolean();
        strip[right ? landing + 1 : landing - 1] = better(random);
        if (random.nextBoolean()) {
            strip[right ? landing - 1 : landing + 1] = better(random);
        }
    }

    private Rarity better(RandomGenerator random) {
        Rarity[] all = Rarity.values();
        return all[landed.ordinal() + 1 + random.nextInt(all.length - landed.ordinal() - 1)];
    }

    /** The Rarity the reel lands on. */
    Rarity landed() {
        return landed;
    }

    /** Opens the menu and starts the reel. */
    void start() {
        open(player);
        if (player.getOpenInventory().getTopInventory().getHolder() != this) {
            complete(); // the menu could not open: never lose the loot
            return;
        }
        play(ReelStyle.open());
        schedule();
    }

    boolean isDone() {
        return done;
    }

    boolean isFor(Player other) {
        return player.getUniqueId().equals(other.getUniqueId());
    }

    /** Ticks to wait before the next move: starts fast, slows down (total {@code 50} for 24 moves). */
    static int delay(int step) {
        return 1 + step * step * step / 2200;
    }

    private void schedule() {
        later(delay(step), this::advance);
    }

    private void advance() {
        step++;
        draw();
        play(List.of(ReelStyle.tick(step, STEPS)));
        if (step < STEPS) {
            schedule();
        } else {
            play(List.of(ReelStyle.land()));
            later(LAND_PAUSE, this::reveal);
        }
    }

    private BukkitTask later(int ticks, Runnable action) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, action, ticks);
        tasks.add(task);
        return task;
    }

    private void play(List<ReelStyle.Cue> cues) {
        for (ReelStyle.Cue cue : cues) {
            if (cue.delay() == 0) {
                playNow(cue);
            } else {
                later(cue.delay(), () -> playNow(cue));
            }
        }
    }

    private void playNow(ReelStyle.Cue cue) {
        if (player.isOnline()) {
            player.playSound(player.getLocation(), cue.sound(), cue.volume(), cue.pitch());
        }
    }

    /** The black frame, with the two pointers. */
    private void frame() {
        for (int slot = 0; slot < size(); slot++) {
            set(slot, new Button(ReelStyle.frame(), p -> {}));
        }
        set(POINTER, Button.of(Material.HOPPER, Component.empty(), p -> {}));
        set(POINTER_BELOW, Button.of(Material.END_ROD, Component.empty(), p -> {}));
    }

    private void draw() {
        for (int i = 0; i < ROW; i++) {
            boolean landing = step == STEPS && i == POINTER;
            set(ROW + i, new Button(ReelStyle.pane(ReelStyle.pane(strip[step + i]), landing), p -> {}));
        }
    }

    /** Gives the loot (once), flashes the whole menu in the landed colour, then pulses and shows the result. */
    private void reveal() {
        give();
        play(ReelStyle.reveal(landed));
        flash();
        later(FLASH, this::result);
        if (landed.ordinal() >= Rarity.EPIC.ordinal()) {
            later(2 * FLASH, this::flash);
            later(3 * FLASH, this::result);
        }
    }

    private void flash() {
        ItemStack pane = ReelStyle.pane(ReelStyle.pane(landed), false);
        for (int slot = 0; slot < size(); slot++) {
            set(slot, new Button(pane.clone(), p -> {}));
        }
    }

    /** Black frame, border in the landed colour, and every Drop with colour bars above and below. */
    private void result() {
        frame();
        for (int slot : BORDER) {
            set(slot, new Button(ReelStyle.pane(ReelStyle.pane(landed), false), p -> {}));
        }
        if (drops.isEmpty() || drops.size() > ROW) {
            ItemStack icon = new ItemStack(Material.NETHER_STAR);
            List<Component> lore = new ArrayList<>();
            for (Drop drop : drops) {
                lore.add(drop.describe().decoration(TextDecoration.ITALIC, false));
            }
            icon.editMeta(meta -> {
                meta.displayName(Text.translatable("rune.cache.reel.result", "Cache opened!")
                        .color(landed.color())
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            bar(CENTER, landed);
            set(CENTER, new Button(icon, p -> {}));
            return;
        }
        int first = (ROW - drops.size()) / 2;
        for (int i = 0; i < drops.size(); i++) {
            Drop drop = drops.get(i);
            Rarity rarity = drop.rarity() == null ? Rarity.COMMON : drop.rarity();
            bar(ROW + first + i, rarity);
            set(ROW + first + i, new Button(drop.icon(), p -> {}));
        }
    }

    /** Colour bars in the rows above and below {@code slot}. */
    private void bar(int slot, Rarity rarity) {
        ItemStack pane = ReelStyle.pane(ReelStyle.pane(rarity), false);
        set(slot - ROW, new Button(pane, p -> {}));
        set(slot + ROW, new Button(pane.clone(), p -> {}));
    }

    private void give() {
        if (!done) {
            done = true;
            finish.run();
        }
    }

    /** Gives the loot (once) and stops the animation. Called when the menu is closed, on quit and on shutdown. */
    void complete() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
        give();
    }
}
