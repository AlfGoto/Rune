package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.jobs.Numbers;
import com.alfgoto.rune.gameplay.runes.PlayerCapacity;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import com.alfgoto.rune.visuals.menu.PagedMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** The Rune Codex menus: the main menu and the Jobs page. They only read Gameplay data. */
public final class CodexMenus {

    static final int BAR_LENGTH = 20;

    private final Registry<Job> jobs;
    private final JobProgress progress;
    private final PlayerCapacity capacity;
    private final PlayerLoadout loadout;
    private final RuneRegistry runes;

    public CodexMenus(
            Registry<Job> jobs,
            JobProgress progress,
            PlayerCapacity capacity,
            PlayerLoadout loadout,
            RuneRegistry runes) {
        this.jobs = jobs;
        this.progress = progress;
        this.capacity = capacity;
        this.loadout = loadout;
        this.runes = runes;
    }

    /** The main menu: the Jobs page and the Runes page (Quests comes with its own ticket). */
    public Menu main(Player player) {
        Menu main = new Menu(Text.translatable("rune.menu.codex.title", "Rune Codex"), 3);
        main.set(
                11,
                Button.of(
                        Material.EXPERIENCE_BOTTLE,
                        Text.translatable("rune.menu.codex.jobs", "Jobs").color(NamedTextColor.GOLD),
                        p -> jobsPage(p, main).open(p)));
        main.set(
                15,
                Button.of(
                        Material.AMETHYST_SHARD,
                        Text.translatable("rune.menu.codex.runes", "Runes").color(NamedTextColor.LIGHT_PURPLE),
                        p -> runesPage(p, main).open(p)));
        return main;
    }

    /** The Runes page: Player Rune slots, Capacity used / max, and the Runes in the inventory. */
    public Menu runesPage(Player player, Menu parent) {
        return new RunesMenu(player, parent, loadout, runes);
    }

    /** The Jobs page: Account Level and Player Capacity first, then each Job with its level and XP bar. */
    public Menu jobsPage(Player player, Menu parent) {
        UUID id = player.getUniqueId();
        List<Button> buttons = new ArrayList<>();
        buttons.add(accountButton(id));
        for (Job job : jobs.all()) {
            buttons.add(jobButton(id, job));
        }
        return new PagedMenu(Text.translatable("rune.menu.jobs.title", "Jobs"), 4, parent, buttons);
    }

    private Button accountButton(UUID id) {
        int level = progress.accountLevel(id);
        LevelCurve curve = progress.accountCurve();
        double xp = progress.accountXp(id);
        ItemStack icon = new ItemStack(Material.NETHER_STAR);
        icon.editMeta(meta -> {
            meta.displayName(plain(Text.translatable("rune.menu.jobs.account", "Account Level %s", Component.text(level))
                    .color(NamedTextColor.AQUA)));
            meta.lore(List.of(
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
                            .color(NamedTextColor.GRAY))));
        });
        return new Button(icon, p -> {});
    }

    private Button jobButton(UUID id, Job job) {
        int level = progress.jobLevel(id, job);
        LevelCurve curve = progress.jobCurve();
        double xp = progress.jobXp(id, job);
        ItemStack icon = new ItemStack(Material.EXPERIENCE_BOTTLE, Math.min(level, 64));
        icon.editMeta(meta -> {
            meta.displayName(plain(Component.translatable(job.translationKey(), job.fallbackName())
                    .color(NamedTextColor.GOLD)));
            meta.lore(List.of(
                    plain(Text.translatable("rune.menu.jobs.level", "Level %s", Component.text(level))
                            .color(NamedTextColor.GREEN)),
                    bar(xp, curve, level),
                    plain(Text.translatable(
                                    "rune.menu.jobs.xp",
                                    "%s / %s XP",
                                    Component.text(Numbers.format(xp)),
                                    Component.text(Numbers.format(curve.totalXpForLevel(level + 1))))
                            .color(NamedTextColor.GRAY))));
        });
        return new Button(icon, p -> {});
    }

    /** A bar of {@link #BAR_LENGTH} squares: the share of the current level already earned. */
    static Component bar(double totalXp, LevelCurve curve, int level) {
        double earned = totalXp - curve.totalXpForLevel(level);
        double fraction = Math.max(0, Math.min(1, earned / curve.xpToNextLevel(level)));
        int filled = (int) Math.floor(fraction * BAR_LENGTH);
        return plain(Component.text("|".repeat(filled), NamedTextColor.GREEN)
                .append(Component.text("|".repeat(BAR_LENGTH - filled), NamedTextColor.DARK_GRAY)));
    }

    private static Component plain(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
