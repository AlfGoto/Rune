package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import com.alfgoto.rune.visuals.menu.MenuBackground;
import com.alfgoto.rune.visuals.menu.MenuCanvas;
import com.alfgoto.rune.visuals.menu.MenuCanvas.Align;
import com.alfgoto.rune.visuals.menu.MenuCanvas.Bar;
import com.alfgoto.rune.visuals.menu.MenuFont;
import com.alfgoto.rune.visuals.menu.MenuImage;
import com.alfgoto.rune.visuals.menu.MenuStrings;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The Jobs page of the Rune Codex: the Account Level banner, then one card per Job (icon, level, XP bar,
 * name), 6 per page. Every planned Job has a card: the ones not playable yet are grey and locked. Layout
 * positions match {@code tools/texture-gen/v3/menus.py} ("Jobs: job cards").
 */
final class JobsMenu extends Menu {

    /** Top-left slot (the icon) of each card; a card is 3 columns x 2 rows. */
    static final int[] CARD_SLOTS = {9, 12, 15, 27, 30, 33};

    static final int PREVIOUS_SLOT = 48;
    static final int NEXT_SLOT = 50;

    /** Every planned Job, in the order of docs/design-decisions.md; the ones not registered yet show locked. */
    static final List<String> PLANNED = List.of(
            "mining",
            "excavation",
            "woodcutting",
            "farming",
            "ranching",
            "fishing",
            "combat",
            "archery",
            "smithing",
            "alchemy",
            "adventuring",
            "runesmithing");

    private static final TextColor LEVEL = TextColor.color(255, 230, 120);
    private static final TextColor LOCKED = TextColor.color(170, 170, 170);

    /** A card: a registered Job, or the id of a planned one (job == null). */
    private record Card(String id, Job job) {}

    private final Player player;
    private final CodexMenus menus;
    private final List<Card> cards = new ArrayList<>();
    private int page;

    JobsMenu(Player player, Menu parent, CodexMenus menus, List<Job> jobs) {
        super(Component.empty(), MenuBackground.ROWS, parent);
        this.player = player;
        this.menus = menus;
        for (String id : PLANNED) {
            cards.add(new Card(id, jobs.stream().filter(j -> j.id().equals(id)).findFirst().orElse(null)));
        }
        for (Job job : jobs) {
            if (!PLANNED.contains(job.id())) {
                cards.add(new Card(job.id(), job));
            }
        }
        draw();
    }

    /** The icon slot of {@code job}'s card on the current page, or -1. */
    int cardSlot(Job job) {
        int from = page * CARD_SLOTS.length;
        for (int k = 0; k < CARD_SLOTS.length && from + k < cards.size(); k++) {
            if (cards.get(from + k).job() == job) {
                return CARD_SLOTS[k];
            }
        }
        return -1;
    }

    @Override
    protected Button backButton() {
        return Button.invisible(Text.translatable("rune.menu.back", "Back").color(NamedTextColor.YELLOW), List.of(),
                this::back);
    }

    int pageCount() {
        return Math.max(1, (cards.size() + CARD_SLOTS.length - 1) / CARD_SLOTS.length);
    }

    void setPage(int page) {
        this.page = Math.max(0, Math.min(page, pageCount() - 1));
        draw();
    }

    private void draw() {
        clear();
        UUID id = player.getUniqueId();
        MenuStrings text = MenuStrings.of(player);
        JobProgress progress = menus.progress();
        int level = progress.accountLevel(id);
        LevelCurve curve = progress.accountCurve();
        double xp = progress.accountXp(id);

        MenuCanvas canvas = MenuCanvas.on(MenuBackground.JOBS)
                .text(88, 6, text.get("rune.menu.jobs.title", "Jobs"), NamedTextColor.WHITE, MenuFont.SHADOW,
                        Align.CENTER, 120)
                .text(27, 21, text.get("rune.menu.jobs.account", "Account Level %s", level), NamedTextColor.WHITE,
                        MenuFont.SHADOW, Align.LEFT, 89)
                .bar(119, 23, 46, CodexMenus.share(xp, curve, level), Bar.AQUA);
        Component accountName = CodexMenus.plain(
                Text.translatable("rune.menu.jobs.account", "Account Level %s", Component.text(level))
                        .color(NamedTextColor.AQUA));
        Button account = Button.invisible(accountName, menus.accountLore(level, xp, curve), p -> {});
        for (int slot = 0; slot < 9; slot++) {
            set(slot, account);
        }

        int from = page * CARD_SLOTS.length;
        for (int k = 0; k < CARD_SLOTS.length; k++) {
            int first = CARD_SLOTS[k];
            if (from + k >= cards.size()) {
                set(first, Button.withModel(Button.EMPTY,
                        Text.translatable("rune.menu.jobs.soon", "Coming soon").color(NamedTextColor.GRAY),
                        List.of(), p -> {}));
                continue;
            }
            Card card = cards.get(from + k);
            int x0 = 7 + 18 * (first % 9);
            int y0 = 17 + 18 * (first / 9);
            boolean top = k < 3;
            if (card.job() == null) {
                lockedCard(canvas, text, card.id(), first, x0, y0, top);
            } else {
                activeCard(canvas, text, card.job(), id, first, x0, y0, top);
            }
        }

        if (page > 0) {
            canvas.image(MenuImage.JOBS_PREVIOUS, 7 + 18 * (PREVIOUS_SLOT % 9));
            set(PREVIOUS_SLOT, Button.invisible(Text.translatable("rune.menu.previous", "Previous page")
                    .color(NamedTextColor.YELLOW), List.of(), p -> setPage(page - 1)));
        }
        if (page < pageCount() - 1) {
            canvas.image(MenuImage.JOBS_NEXT, 7 + 18 * (NEXT_SLOT % 9));
            set(NEXT_SLOT, Button.invisible(Text.translatable("rune.menu.next", "Next page")
                    .color(NamedTextColor.YELLOW), List.of(), p -> setPage(page + 1)));
        }
        retitle(canvas.build());
    }

    private void activeCard(
            MenuCanvas canvas, MenuStrings text, Job job, UUID id, int first, int x0, int y0, boolean top) {
        JobProgress progress = menus.progress();
        int jobLevel = progress.jobLevel(id, job);
        canvas.image(top ? MenuImage.JOB_CARD_TOP : MenuImage.JOB_CARD_BOTTOM, x0)
                .text(x0 + 21, y0 + 5, text.get("rune.menu.jobs.card_level", "Lv %s", jobLevel), LEVEL,
                        MenuFont.SHADOW, Align.LEFT, 33)
                .bar(x0 + 21, y0 + 16, 31,
                        CodexMenus.share(progress.jobXp(id, job), progress.jobCurve(), jobLevel), Bar.GREEN)
                .text(x0 + 3, y0 + 23, text.get(job.translationKey(), job.fallbackName()), NamedTextColor.WHITE,
                        MenuFont.SHADOW, Align.LEFT, 49);
        Component name = CodexMenus.plain(
                Component.translatable(job.translationKey(), job.fallbackName()).color(NamedTextColor.GOLD));
        List<Component> lore = menus.jobLore(id, job);
        ItemStack icon = new ItemStack(icon(job));
        icon.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(lore);
        });
        fill(first, new Button(icon, p -> {}), Button.invisible(name, lore, p -> {}));
    }

    private void lockedCard(MenuCanvas canvas, MenuStrings text, String jobId, int first, int x0, int y0, boolean top) {
        String key = Text.key("job", jobId);
        String fallback = Character.toUpperCase(jobId.charAt(0)) + jobId.substring(1);
        canvas.image(top ? MenuImage.JOB_CARD_TOP_LOCKED : MenuImage.JOB_CARD_BOTTOM_LOCKED, x0)
                .text(x0 + 3, y0 + 23, text.get(key, fallback), LOCKED, MenuFont.SHADOW, Align.LEFT, 49);
        Component name = CodexMenus.plain(Component.translatable(key, fallback).color(NamedTextColor.GRAY));
        List<Component> lore = List.of(CodexMenus.plain(
                Text.translatable("rune.menu.jobs.locked", "Not playable yet").color(NamedTextColor.DARK_GRAY)));
        fill(first, Button.withModel(new NamespacedKey("rune", "menu_locked_" + jobId), name, lore, p -> {}),
                Button.invisible(name, lore, p -> {}));
    }

    /** {@code icon} in the card's top-left slot, {@code rest} on its 5 other slots. */
    private void fill(int first, Button icon, Button rest) {
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = first + row * 9 + col;
                set(slot, slot == first ? icon : rest);
            }
        }
    }

    /** The tool shown on a Job's card. */
    static Material icon(Job job) {
        return switch (job.id()) {
            case "mining" -> Material.IRON_PICKAXE;
            case "woodcutting" -> Material.IRON_AXE;
            case "excavation" -> Material.IRON_SHOVEL;
            case "farming" -> Material.IRON_HOE;
            case "fishing" -> Material.FISHING_ROD;
            case "combat" -> Material.IRON_SWORD;
            case "archery" -> Material.BOW;
            default -> Material.EXPERIENCE_BOTTLE;
        };
    }
}
