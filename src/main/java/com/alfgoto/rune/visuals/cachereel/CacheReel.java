package com.alfgoto.rune.visuals.cachereel;

import com.alfgoto.rune.gameplay.caches.CacheOpenEvent;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.text.Rarity;
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
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Cache reel (docs/art-direction.md, "Menus"): when a Cache is opened, a row of items spins under a
 * pointer, slows down and stops on the first reward; then the frame lights up in the Cache's Rarity colour
 * and every reward shows in the row. Only shows: the loot is given by Gameplay ({@code Caches}) when the
 * reel ends, or at once if the player leaves. Closing the reel early changes nothing.
 */
final class CacheReel extends Menu {

    /** The reel's row and its centre (under the pointer). */
    static final int FIRST = 18;

    static final int CENTER = 22;
    static final int LENGTH = 9;

    /** Ticks between two moves of the reel: fast, then slowing down (45 ticks in all). */
    static final int[] DELAYS = {1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 4, 5, 6, 8};

    private static final TextColor HINT = TextColor.color(150, 150, 150);

    private final Player player;
    private final CacheOpenEvent opened;
    private final MenuStrings text;
    private final List<ItemStack> strip = new ArrayList<>();
    private int offset;
    private int step;
    private int waited;
    private boolean revealed;
    private BukkitTask task;

    /** {@code filler} gives the items that spin past before the reward (shown only). */
    CacheReel(CacheOpenEvent opened, Supplier<ItemStack> filler) {
        super(Component.empty(), MenuBackground.ROWS);
        this.player = opened.player();
        this.opened = opened;
        this.text = MenuStrings.of(player);
        for (int i = 0; i < DELAYS.length + LENGTH; i++) {
            strip.add(filler.get());
        }
        // After the last move, the centre shows the first reward.
        strip.set(DELAYS.length + (CENTER - FIRST), opened.drops().get(0).icon());
        retitle(canvas().build());
        drawRow();
    }

    /** Opens the reel and starts spinning. */
    void start(Plugin plugin) {
        open(player);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    boolean revealed() {
        return revealed;
    }

    /** One tick: move when the current delay is over; stop when the player closed the reel or left. */
    void tick() {
        if (revealed || !player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != this) {
            stop();
            return;
        }
        if (++waited < DELAYS[step]) {
            return;
        }
        waited = 0;
        step++;
        offset++;
        drawRow();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 0.8f + 0.06f * step);
        if (step >= DELAYS.length) {
            reveal();
        }
    }

    private void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void drawRow() {
        for (int i = 0; i < LENGTH; i++) {
            set(FIRST + i, new Button(strip.get(offset + i).clone(), p -> {}));
        }
    }

    /** The rewards, centred on the row, and the frame lit in the Rarity colour. */
    private void reveal() {
        revealed = true;
        stop();
        List<Drop> drops = opened.drops();
        int shown = Math.min(LENGTH, drops.size());
        int start = CENTER - FIRST - (shown - 1) / 2;
        for (int i = 0; i < LENGTH; i++) {
            int index = i - start;
            set(FIRST + i, index >= 0 && index < shown
                    ? new Button(drops.get(index).icon(), p -> {})
                    : Button.withModel(Button.EMPTY, Component.empty(), List.of(), p -> {}));
        }
        retitle(canvas()
                .image(MenuImage.glow(opened.rarity()), 4)
                .text(88, 86, text.get("rune.menu.cache.rewards", "Rewards"), NamedTextColor.WHITE, MenuFont.SHADOW,
                        Align.CENTER, 120)
                .text(88, 100, text.get("rune.menu.cache.close", "Press Esc to close"), HINT, MenuFont.SMALL,
                        Align.CENTER, 120)
                .build());
        player.playSound(player.getLocation(), sound(opened.rarity()), 1f, 1f);
        if (opened.rarity() == Rarity.LEGENDARY) {
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1f);
        }
    }

    private MenuCanvas canvas() {
        Rarity rarity = opened.rarity();
        MenuImage cache = MenuImage.cache(rarity);
        String name = text.get(rarity.translationKey(), rarity.englishName());
        return MenuCanvas.on(MenuBackground.REEL)
                .image(cache, 88 - cache.width() / 2)
                .text(88, 6, text.get("rune.menu.cache.title", "%s Cache", name), rarity.color(), MenuFont.SHADOW,
                        Align.CENTER, 150);
    }

    static Sound sound(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            case UNCOMMON, RARE -> Sound.ENTITY_PLAYER_LEVELUP;
            case EPIC -> Sound.BLOCK_BEACON_ACTIVATE;
            case LEGENDARY -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
        };
    }
}
