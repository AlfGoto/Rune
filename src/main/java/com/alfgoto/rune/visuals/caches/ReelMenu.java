package com.alfgoto.rune.visuals.caches;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import com.alfgoto.rune.visuals.menu.MenuBackground;
import com.alfgoto.rune.visuals.menu.MenuCanvas;
import com.alfgoto.rune.visuals.menu.MenuCanvas.Align;
import com.alfgoto.rune.visuals.menu.MenuFont;
import com.alfgoto.rune.visuals.menu.MenuImage;
import com.alfgoto.rune.visuals.menu.MenuStrings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Cache reel, a drawn menu (docs/art-direction.md, "Menus"): the Cache and its name on top, a row of
 * items that scrolls under the pointer, slows down and lands CS:GO style on the best Rarity among the
 * loot (with a near miss next to it). Then the frame glows in that Rarity colour, a sound plays and every
 * Drop shows in the row. The loot is given once: at the reveal, or when the player closes the menu early.
 */
final class ReelMenu extends Menu {

    static final int STEPS = 24;
    /** Ticks between the landing and the reveal. */
    static final int LAND_PAUSE = 8;

    /** The reel's row (slots 18 to 26) and its centre, under the drawn pointer. */
    static final int FIRST = 18;

    static final int CENTER = 22;
    static final int LENGTH = 9;

    private static final int POINTER = CENTER - FIRST;
    private static final TextColor HINT = TextColor.color(150, 150, 150);

    private final Plugin plugin;
    private final Player player;
    private final Rarity cache;
    private final Rarity landed;
    private final List<Drop> drops;
    private final Runnable finish;
    private final MenuStrings text;
    private final ItemStack[] strip = new ItemStack[STEPS + LENGTH];
    private final List<BukkitTask> tasks = new ArrayList<>();
    private int step;
    private boolean done;
    private boolean retitling;

    /** {@code filler} gives an item of a Rarity to spin past (shown only, never given). */
    ReelMenu(
            Plugin plugin,
            Player player,
            Rarity cache,
            List<Drop> drops,
            Runnable finish,
            RandomGenerator random,
            Function<Rarity, ItemStack> filler) {
        super(Component.empty(), MenuBackground.ROWS);
        this.plugin = plugin;
        this.player = player;
        this.cache = cache;
        this.drops = drops;
        this.finish = finish;
        this.text = MenuStrings.of(player);
        this.landed = ReelStyle.landed(drops);
        Rarity[] rarities = new Rarity[strip.length];
        for (int i = 0; i < rarities.length; i++) {
            rarities[i] = ReelStyle.weighted(random);
        }
        int landing = STEPS + POINTER; // the item under the pointer after the last move
        rarities[landing] = landed;
        nearMiss(rarities, landing, random);
        for (int i = 0; i < strip.length; i++) {
            strip[i] = filler.apply(rarities[i]);
        }
        strip[landing] = landingIcon(filler);
        retitle(canvas().build());
        draw();
    }

    /** Puts 1 or 2 better items next to the landing item, unless it is already Legendary. */
    private void nearMiss(Rarity[] rarities, int landing, RandomGenerator random) {
        if (landed == Rarity.LEGENDARY) {
            return;
        }
        boolean right = random.nextBoolean();
        rarities[right ? landing + 1 : landing - 1] = better(random);
        if (random.nextBoolean()) {
            rarities[right ? landing - 1 : landing + 1] = better(random);
        }
    }

    private Rarity better(RandomGenerator random) {
        Rarity[] all = Rarity.values();
        return all[landed.ordinal() + 1 + random.nextInt(all.length - landed.ordinal() - 1)];
    }

    /** The first Drop of the landed Rarity, else the first Drop, else a filler of that Rarity. */
    private ItemStack landingIcon(Function<Rarity, ItemStack> filler) {
        for (Drop drop : drops) {
            if (drop.rarity() == landed) {
                return drop.icon();
            }
        }
        return drops.isEmpty() ? filler.apply(landed) : drops.get(0).icon();
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

    /** True while the menu reopens itself to change its drawn title: that close is not the player's. */
    boolean isRetitling() {
        return retitling;
    }

    @Override
    protected void retitle(Component title) {
        retitling = true;
        try {
            super.retitle(title);
        } finally {
            retitling = false;
        }
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

    private void draw() {
        for (int i = 0; i < LENGTH; i++) {
            ItemStack item = strip[step + i].clone();
            if (step == STEPS && i == POINTER) {
                item.editMeta(meta -> meta.setEnchantmentGlintOverride(true));
            }
            set(FIRST + i, new Button(item, p -> {}));
        }
    }

    /** Gives the loot (once), lights the frame in the landed colour and shows every Drop in the row. */
    private void reveal() {
        give();
        play(ReelStyle.reveal(landed));
        if (drops.isEmpty() || drops.size() > LENGTH) {
            for (int i = 0; i < LENGTH; i++) {
                set(FIRST + i, empty());
            }
            set(CENTER, new Button(summary(), p -> {}));
        } else {
            int first = POINTER - (drops.size() - 1) / 2;
            for (int i = 0; i < LENGTH; i++) {
                int index = i - first;
                set(FIRST + i, index >= 0 && index < drops.size()
                        ? new Button(drops.get(index).icon(), p -> {})
                        : empty());
            }
        }
        retitle(canvas()
                .image(MenuImage.glow(landed), 4)
                .text(88, 86, text.get("rune.menu.cache.rewards", "Rewards"), NamedTextColor.WHITE, MenuFont.SHADOW,
                        Align.CENTER, 120)
                .text(88, 100, text.get("rune.menu.cache.close", "Press Esc to close"), HINT, MenuFont.SMALL,
                        Align.CENTER, 120)
                .build());
    }

    private static Button empty() {
        return Button.withModel(Button.EMPTY, Component.empty(), List.of(), p -> {});
    }

    /** No Drop, or more than fit in the row: one Nether Star with the loot in its lore. */
    private ItemStack summary() {
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
        return icon;
    }

    /** The drawn reel, with the Cache and its name in the Cache's Rarity colour. */
    private MenuCanvas canvas() {
        MenuImage image = MenuImage.cache(cache);
        String name = text.get(cache.translationKey(), cache.englishName());
        return MenuCanvas.on(MenuBackground.REEL)
                .image(image, 88 - image.width() / 2)
                .text(88, 6, text.get("rune.menu.cache.title", "%s Cache", name), cache.color(), MenuFont.SHADOW,
                        Align.CENTER, 150);
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
