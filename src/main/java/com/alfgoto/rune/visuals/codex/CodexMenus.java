package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.jobs.Numbers;
import com.alfgoto.rune.gameplay.quests.Quest;
import com.alfgoto.rune.gameplay.quests.QuestBook;
import com.alfgoto.rune.gameplay.runes.PlayerCapacity;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import com.alfgoto.rune.visuals.menu.MenuBackground;
import com.alfgoto.rune.visuals.menu.MenuCanvas;
import com.alfgoto.rune.visuals.menu.MenuCanvas.Align;
import com.alfgoto.rune.visuals.menu.MenuCanvas.Bar;
import com.alfgoto.rune.visuals.menu.MenuFont;
import com.alfgoto.rune.visuals.menu.MenuStrings;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * The Rune Codex menus. The main menu is an open book (docs/art-direction.md, "Menus"): the left page shows
 * the player (head, name, Account Level, XP, Capacity), the right page has three chapter buttons that lead
 * to the Jobs, Quests and Runes pages. Layout positions match {@code tools/texture-gen/v3/menus.py}.
 */
public final class CodexMenus {

    static final int BAR_LENGTH = 20;

    /** The player's head on the left page. */
    static final int PORTRAIT_SLOT = 11;

    /** Top-left slot of each chapter button (4 columns x 2 rows): Jobs, Quests, Runes. */
    static final int JOBS_FIRST = 5;

    static final int QUESTS_FIRST = 23;
    static final int RUNES_FIRST = 41;

    static final TextColor INK = TextColor.color(74, 52, 30);
    static final TextColor INK_LIGHT = TextColor.color(110, 90, 60);
    static final TextColor LEVEL_BLUE = TextColor.color(40, 90, 160);
    static final TextColor CHAPTER_VALUE = TextColor.color(235, 225, 200);

    private final Registry<Job> jobs;
    private final JobProgress progress;
    private final PlayerCapacity capacity;
    private final QuestBook quests;
    private final RuneConfig config;
    private final PlayerLoadout loadout;
    private final RuneRegistry runes;

    public CodexMenus(
            Registry<Job> jobs,
            JobProgress progress,
            PlayerCapacity capacity,
            QuestBook quests,
            RuneConfig config,
            PlayerLoadout loadout,
            RuneRegistry runes) {
        this.jobs = jobs;
        this.progress = progress;
        this.capacity = capacity;
        this.quests = quests;
        this.config = config;
        this.loadout = loadout;
        this.runes = runes;
    }

    /** The Quests page: the 3 active Quests as notes in their Tier colour, a reroll button under each. */
    public QuestsMenu questsPage(Player player, Menu parent) {
        return new QuestsMenu(quests, config, player, parent);
    }

    /** The Runes page: the Player Rune sockets around the Capacity orb, and the Runes in the inventory. */
    public Menu runesPage(Player player, Menu parent) {
        return new RunesMenu(player, parent, loadout, runes);
    }

    /** The Jobs page: Account Level, then one card per Job with its level and XP. */
    public Menu jobsPage(Player player, Menu parent) {
        return new JobsMenu(player, parent, this, List.copyOf(jobs.all()));
    }

    /** The main menu: the open book. */
    public Menu main(Player player) {
        UUID id = player.getUniqueId();
        MenuStrings text = MenuStrings.of(player);
        int level = progress.accountLevel(id);
        LevelCurve curve = progress.accountCurve();
        double xp = progress.accountXp(id);
        long used = loadout.used(id);
        int max = loadout.capacity(id);
        int bestJob = jobs.all().stream().mapToInt(job -> progress.jobLevel(id, job)).max().orElse(0);
        Map<Integer, Quest> active = quests.slots(id);
        long done = active.entrySet().stream()
                .filter(e -> quests.progress(id, e.getKey()) >= e.getValue().target())
                .count();
        int installed = loadout.installed(id).size();

        MenuCanvas canvas = MenuCanvas.on(MenuBackground.CODEX)
                .text(48, 11, text.get("rune.menu.codex.title", "Rune Codex"), INK, MenuFont.NORMAL, Align.CENTER, 70)
                .text(48, 58, player.getName(), INK, MenuFont.NORMAL, Align.CENTER, 70)
                .text(48, 69, text.get("rune.menu.codex.level", "Level %s", level), LEVEL_BLUE, MenuFont.NORMAL,
                        Align.CENTER, 70)
                .bar(19, 82, 59, share(xp, curve, level), Bar.GREEN)
                .text(48, 89, Numbers.format(xp) + " / " + Numbers.format(curve.totalXpForLevel(level + 1)), INK_LIGHT,
                        MenuFont.SMALL, Align.CENTER, 70)
                .text(48, 99, text.get("rune.menu.codex.capacity", "Capacity"), INK, MenuFont.NORMAL, Align.CENTER, 70)
                .bar(19, 111, 59, max <= 0 ? 0 : (double) used / max, Bar.PURPLE)
                .text(48, 117, used + " / " + max, INK_LIGHT, MenuFont.SMALL, Align.CENTER, 70);
        chapterText(canvas, JOBS_FIRST, text.get("rune.menu.codex.jobs", "Jobs"),
                text.get("rune.menu.codex.jobs_value", "Lv %s", bestJob));
        chapterText(canvas, QUESTS_FIRST, text.get("rune.menu.codex.quests", "Quests"), done + " / " + active.size());
        chapterText(canvas, RUNES_FIRST, text.get("rune.menu.codex.runes", "Runes"),
                installed + " / " + PlayerLoadout.SLOTS);

        Menu main = new Menu(canvas.build(), MenuBackground.ROWS);
        main.set(PORTRAIT_SLOT, portrait(player, level, xp, curve));
        chapter(main, JOBS_FIRST, Text.translatable("rune.menu.codex.jobs", "Jobs").color(NamedTextColor.GOLD),
                p -> jobsPage(p, main).open(p));
        chapter(main, QUESTS_FIRST, Text.translatable("rune.menu.codex.quests", "Quests").color(NamedTextColor.GOLD),
                p -> questsPage(p, main).open(p));
        chapter(main, RUNES_FIRST,
                Text.translatable("rune.menu.codex.runes", "Runes").color(NamedTextColor.LIGHT_PURPLE),
                p -> runesPage(p, main).open(p));
        return main;
    }

    /** The 8 slots of a chapter button, from its top-left slot. */
    static int[] chapterSlots(int first) {
        int[] slots = new int[8];
        for (int i = 0; i < 8; i++) {
            slots[i] = first + (i / 4) * 9 + (i % 4);
        }
        return slots;
    }

    private static void chapterText(MenuCanvas canvas, int first, String label, String value) {
        int x0 = 7 + 18 * (first % 9);
        int y0 = 17 + 18 * (first / 9);
        canvas.text(x0 + 25, y0 + 8, label, NamedTextColor.WHITE, MenuFont.SHADOW, Align.LEFT, 44)
                .text(x0 + 25, y0 + 21, value, CHAPTER_VALUE, MenuFont.SMALL, Align.LEFT, 44);
    }

    private static void chapter(Menu menu, int first, Component name, Consumer<Player> open) {
        for (int slot : chapterSlots(first)) {
            menu.set(slot, Button.invisible(name, List.of(), open));
        }
    }

    private Button portrait(Player player, int level, double xp, LevelCurve curve) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        head.editMeta(meta -> {
            if (meta instanceof SkullMeta skull) {
                skull.setOwningPlayer(player);
            }
            meta.displayName(plain(Text.translatable("rune.menu.jobs.account", "Account Level %s", Component.text(level))
                    .color(NamedTextColor.AQUA)));
            meta.lore(accountLore(level, xp, curve));
        });
        return new Button(head, p -> {});
    }

    /** Tooltip lines of the Account Level: Player Capacity, XP bar, XP numbers. */
    List<Component> accountLore(int level, double xp, LevelCurve curve) {
        return List.of(
                plain(Text.translatable(
                                "rune.menu.jobs.capacity",
                                "Player Capacity: %s",
                                Component.text(capacity.forAccountLevel(level)))
                        .color(NamedTextColor.GREEN)),
                bar(xp, curve, level),
                plain(Text.translatable(
                                "rune.menu.jobs.account_xp",
                                "%s / %s Account XP",
                                Component.text(Numbers.format(xp)),
                                Component.text(Numbers.format(curve.totalXpForLevel(level + 1))))
                        .color(NamedTextColor.GRAY)));
    }

    /** Tooltip lines of a Job: level, XP bar, XP numbers. */
    List<Component> jobLore(UUID id, Job job) {
        int level = progress.jobLevel(id, job);
        LevelCurve curve = progress.jobCurve();
        double xp = progress.jobXp(id, job);
        return List.of(
                plain(Text.translatable("rune.menu.jobs.level", "Level %s", Component.text(level))
                        .color(NamedTextColor.GREEN)),
                bar(xp, curve, level),
                plain(Text.translatable(
                                "rune.menu.jobs.xp",
                                "%s / %s XP",
                                Component.text(Numbers.format(xp)),
                                Component.text(Numbers.format(curve.totalXpForLevel(level + 1))))
                        .color(NamedTextColor.GRAY)));
    }

    JobProgress progress() {
        return progress;
    }

    /** The share of the current level already earned, 0..1. */
    static double share(double totalXp, LevelCurve curve, int level) {
        double earned = totalXp - curve.totalXpForLevel(level);
        return Math.max(0, Math.min(1, earned / curve.xpToNextLevel(level)));
    }

    /** A bar of {@link #BAR_LENGTH} squares: the share of the current level already earned (for tooltips). */
    static Component bar(double totalXp, LevelCurve curve, int level) {
        int filled = (int) Math.floor(share(totalXp, curve, level) * BAR_LENGTH);
        return plain(Component.text("|".repeat(filled), NamedTextColor.GREEN)
                .append(Component.text("|".repeat(BAR_LENGTH - filled), NamedTextColor.DARK_GRAY)));
    }

    static Component plain(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
