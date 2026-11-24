package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.gameplay.quests.Quest;
import com.alfgoto.rune.gameplay.quests.QuestBook;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.visuals.menu.Button;
import com.alfgoto.rune.visuals.menu.Menu;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The Quests page of the Rune Codex: the 3 active Quests, each with its Tier colour and live progress.
 * Clicking a Quest spends a Reroll Token to replace it. It only reads and acts through {@link QuestBook}.
 */
public final class QuestsMenu extends Menu {

    static final int BAR_LENGTH = 20;
    static final int[] QUEST_SLOTS = {11, 13, 15};
    private static final int TOKEN_SLOT = 4;

    private final QuestBook book;
    private final RuneConfig config;
    private final UUID owner;

    public QuestsMenu(QuestBook book, RuneConfig config, Player player, Menu parent) {
        super(Text.translatable("rune.menu.quests.title", "Quests"), 3, parent);
        this.book = book;
        this.config = config;
        this.owner = player.getUniqueId();
        refresh();
    }

    public UUID owner() {
        return owner;
    }

    /** Redraws the Quests, their progress and the Reroll Token count from the current data. */
    public void refresh() {
        clear();
        set(TOKEN_SLOT, tokenButton(book.rerollTokens().count(owner)));
        for (Map.Entry<Integer, Quest> entry : book.slots(owner).entrySet()) {
            int slot = entry.getKey();
            set(QUEST_SLOTS[slot - 1], questButton(slot, entry.getValue()));
        }
    }

    private Button tokenButton(int tokens) {
        ItemStack icon = new ItemStack(Material.PAPER);
        icon.editMeta(meta -> meta.displayName(plain(Text.translatable(
                        "rune.menu.quests.tokens",
                        "Reroll Tokens: %s / %s",
                        Component.text(tokens),
                        Component.text(config.questMaxRerollTokens()))
                .color(NamedTextColor.AQUA))));
        return new Button(icon, p -> {});
    }

    private Button questButton(int slot, Quest quest) {
        int done = Math.min(book.progress(owner, slot), quest.target());
        ItemStack icon = new ItemStack(material(quest.tier()));
        icon.editMeta(meta -> {
            meta.displayName(plain(Component.translatable(
                            quest.type().translationKey(), quest.type().fallbackName())
                    .color(quest.tier().color())));
            meta.lore(List.of(
                    plain(Component.translatable(
                                    quest.type().job().translationKey(),
                                    quest.type().job().fallbackName())
                            .color(NamedTextColor.GOLD)),
                    plain(Text.translatable("rune.menu.quests.tier", "Tier: %s", Text.rarityLine(quest.tier()))
                            .color(NamedTextColor.GRAY)),
                    bar(done, quest.target()),
                    plain(Text.translatable(
                                    "rune.menu.quests.progress",
                                    "%s / %s",
                                    Component.text(done),
                                    Component.text(quest.target()))
                            .color(NamedTextColor.GRAY)),
                    Component.empty(),
                    plain(Text.translatable("rune.menu.quests.reroll", "Click to reroll (1 Reroll Token)")
                            .color(NamedTextColor.YELLOW))));
        });
        return new Button(icon, p -> reroll(p, slot));
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
        int filled = target <= 0 ? 0 : Math.min(BAR_LENGTH, (int) Math.floor((double) done / target * BAR_LENGTH));
        return plain(Component.text("|".repeat(filled), NamedTextColor.GREEN)
                .append(Component.text("|".repeat(BAR_LENGTH - filled), NamedTextColor.DARK_GRAY)));
    }

    private static Component plain(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
