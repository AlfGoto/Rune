package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.quests.Quest;
import com.alfgoto.rune.gameplay.quests.QuestBook;
import com.alfgoto.rune.text.Rarity;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The Quests page of the Rune Codex: a notice board with the 3 active Quests as notes in their Tier colour
 * (goal, progress bar, Tier), a copper reroll button under each while a Reroll Token can be spent, and the
 * Reroll Tokens. It only reads and acts
 * through {@link QuestBook}. Layout positions match {@code tools/texture-gen/v3/menus.py} ("Quests").
 */
public final class QuestsMenu extends Menu {

    /** The icon of each Quest (a dye in its Tier colour); the rest of the note shows the same tooltip. */
    static final int[] QUEST_SLOTS = {10, 13, 16};

    /** The reroll button under each note. */
    static final int[] REROLL_SLOTS = {46, 49, 52};

    static final int TOKEN_SLOT = 47;
    static final int BACK_SLOT = 53;

    private static final int[] NOTE_X = {9, 63, 117};
    private static final TextColor INK = TextColor.color(74, 52, 30);
    private static final TextColor INK_LIGHT = TextColor.color(110, 90, 60);

    private final QuestBook book;
    private final RuneConfig config;
    private final UUID owner;
    private final MenuStrings text;

    public QuestsMenu(QuestBook book, RuneConfig config, Player player, Menu parent) {
        super(Component.empty(), MenuBackground.ROWS, parent);
        this.book = book;
        this.config = config;
        this.owner = player.getUniqueId();
        this.text = MenuStrings.of(player);
        refresh();
    }

    public UUID owner() {
        return owner;
    }

    @Override
    protected int backSlot() {
        return BACK_SLOT;
    }

    @Override
    protected Button backButton() {
        return Button.invisible(Text.translatable("rune.menu.back", "Back").color(NamedTextColor.YELLOW), List.of(),
                this::back);
    }

    /** Redraws the notes, their progress and the Reroll Token count from the current data. */
    public void refresh() {
        clear();
        int tokens = book.rerollTokens().count(owner);
        MenuCanvas canvas = MenuCanvas.on(MenuBackground.QUESTS)
                .text(63, 113, tokens + " / " + config.questMaxRerollTokens(), NamedTextColor.WHITE, MenuFont.SMALL,
                        Align.LEFT);
        set(TOKEN_SLOT, Button.invisible(tokenName(tokens), List.of(), p -> {}));
        for (Map.Entry<Integer, Quest> entry : book.slots(owner).entrySet()) {
            int slot = entry.getKey();
            if (slot < 1 || slot > QUEST_SLOTS.length) {
                continue;
            }
            note(canvas, slot, entry.getValue(), tokens > 0);
        }
        retitle(canvas.build());
    }

    private void note(MenuCanvas canvas, int slot, Quest quest, boolean canReroll) {
        int k = slot - 1;
        int x0 = NOTE_X[k];
        int done = Math.min(book.progress(owner, slot), quest.target());
        boolean complete = done >= quest.target();
        canvas.image(MenuImage.note(quest.tier()), x0)
                .lines(x0 + 25, 56, 10, 2, text.get(quest.type().translationKey(), quest.type().fallbackName()),
                        INK, MenuFont.NORMAL, Align.CENTER, 46)
                .bar(x0 + 5, 79, 41, quest.target() <= 0 ? 0 : (double) done / quest.target(),
                        complete ? Bar.GOLD : Bar.GREEN)
                .text(x0 + 25, 85, done + " / " + quest.target(), INK_LIGHT, MenuFont.SMALL, Align.CENTER, 46)
                .text(x0 + 25, 95, text.get(quest.tier().translationKey(), quest.tier().englishName()), INK_LIGHT,
                        MenuFont.SMALL, Align.CENTER, 46);

        Component name = CodexMenus.plain(
                Component.translatable(quest.type().translationKey(), quest.type().fallbackName())
                        .color(quest.tier().color()));
        List<Component> lore = List.of(
                CodexMenus.plain(Component.translatable(
                                quest.type().job().translationKey(), quest.type().job().fallbackName())
                        .color(NamedTextColor.GOLD)),
                CodexMenus.plain(Text.translatable("rune.menu.quests.tier", "Tier: %s", Text.rarityLine(quest.tier()))
                        .color(NamedTextColor.GRAY)),
                bar(done, quest.target()),
                CodexMenus.plain(Text.translatable(
                                "rune.menu.quests.progress",
                                "%s / %s",
                                Component.text(done),
                                Component.text(quest.target()))
                        .color(NamedTextColor.GRAY)));
        ItemStack icon = new ItemStack(material(quest.tier()));
        icon.editMeta(meta -> {
            meta.displayName(name);
            meta.lore(lore);
        });
        int first = 3 * k;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 3; col++) {
                int cell = first + row * 9 + col;
                set(cell, cell == QUEST_SLOTS[k] ? new Button(icon, p -> {}) : Button.invisible(name, lore, p -> {}));
            }
        }
        if (!canReroll) {
            return; // no Reroll Token: the button is not shown at all
        }
        canvas.image(MenuImage.REROLL, 7 + 18 * (REROLL_SLOTS[k] % 9));
        set(REROLL_SLOTS[k], Button.invisible(
                Text.translatable("rune.menu.quests.reroll_button", "Reroll this Quest").color(NamedTextColor.GOLD),
                List.of(Text.translatable("rune.menu.quests.reroll_cost", "Costs 1 Reroll Token")
                        .color(NamedTextColor.GRAY)),
                p -> reroll(p, slot)));
    }

    private Component tokenName(int tokens) {
        return Text.translatable(
                        "rune.menu.quests.tokens",
                        "Reroll Tokens: %s / %s",
                        Component.text(tokens),
                        Component.text(config.questMaxRerollTokens()))
                .color(NamedTextColor.AQUA);
    }

    private void reroll(Player player, int slot) {
        if (book.rerollTokens().count(owner) <= 0) {
            player.sendMessage(Text.translatable("rune.command.quest.no_tokens", "You have no Reroll Token.")
                    .color(NamedTextColor.RED));
            return;
        }
        if (book.reroll(owner, slot).isEmpty()) {
            player.sendMessage(Text.translatable("rune.command.quest.none", "No Quest is available yet.")
                    .color(NamedTextColor.RED));
            return;
        }
        refresh();
    }

    /** One coloured dye per Tier, matching the Rarity colour. */
    static Material material(Rarity tier) {
        return switch (tier) {
            case COMMON -> Material.WHITE_DYE;
            case UNCOMMON -> Material.LIME_DYE;
            case RARE -> Material.LIGHT_BLUE_DYE;
            case EPIC -> Material.MAGENTA_DYE;
            case LEGENDARY -> Material.ORANGE_DYE;
        };
    }

    static Component bar(int done, int target) {
        return CodexMenus.bar(target <= 0 ? 0 : Math.min(1, (double) done / target));
    }
}
