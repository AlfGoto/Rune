package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.command.Subcommand;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /rune debug menu}: a test menu with a sub-menu, 60 buttons over several pages, and back buttons. */
public final class MenuDebug implements Subcommand {

    static final int TEST_ITEMS = 60;

    @Override
    public String name() {
        return "menu";
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Text.translatable("rune.menu.player_only", "Only a player can do this.")
                    .color(NamedTextColor.RED));
            return;
        }
        root().open(player);
    }

    Menu root() {
        Menu root = new Menu(Text.translatable("rune.menu.debug.title", "Menu test"), 3);
        root.set(
                11,
                Button.of(
                        Material.BOOK,
                        Text.translatable("rune.menu.debug.pages", "Open the pages"),
                        player -> pages(root).open(player)));
        root.set(
                15,
                Button.of(
                        Material.CHEST,
                        Text.translatable("rune.menu.debug.sub", "Open a sub-menu"),
                        player -> sub(root).open(player)));
        return root;
    }

    private static Menu sub(Menu parent) {
        Menu sub = new Menu(Text.translatable("rune.menu.debug.sub_title", "Sub-menu"), 3, parent);
        sub.set(
                13,
                Button.of(
                        Material.DIAMOND,
                        Text.translatable("rune.menu.debug.click", "Click me"),
                        player -> player.sendMessage(Text.translatable("rune.menu.debug.clicked", "Clicked!"))));
        return sub;
    }

    private static Menu pages(Menu parent) {
        List<Button> items = new ArrayList<>();
        for (int i = 1; i <= TEST_ITEMS; i++) {
            int number = i;
            items.add(Button.of(
                    Material.PAPER,
                    number,
                    Text.translatable("rune.menu.debug.item", "Item %s", Component.text(number)),
                    player -> player.sendMessage(Text.translatable(
                            "rune.menu.debug.item_clicked", "You clicked item %s.", Component.text(number)))));
        }
        return new PagedMenu(Text.translatable("rune.menu.debug.pages_title", "Pages"), 4, parent, items);
    }
}
