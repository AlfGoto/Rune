package com.alfgoto.rune.visuals.menu;

import com.alfgoto.rune.text.Text;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/**
 * A menu that spreads a list of buttons over pages. The last row holds the navigation: previous
 * page, back (if there is a parent), next page. Only the arrows that lead somewhere are shown.
 */
public class PagedMenu extends Menu {

    private final List<Button> items;
    private int page;

    public PagedMenu(Component title, int rows, Menu parent, List<Button> items) {
        super(title, rows, parent);
        if (rows < 2) {
            throw new IllegalArgumentException("a paged menu needs at least 2 rows");
        }
        this.items = List.copyOf(items);
        render();
    }

    /** How many buttons fit on one page. */
    public int pageSize() {
        return size() - 9;
    }

    public int pageCount() {
        return Math.max(1, (items.size() + pageSize() - 1) / pageSize());
    }

    /** The current page, starting at 0. */
    public int page() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, Math.min(page, pageCount() - 1));
        render();
    }

    private void render() {
        clear();
        int from = page * pageSize();
        for (int i = 0; i < pageSize() && from + i < items.size(); i++) {
            set(i, items.get(from + i));
        }
        if (page > 0) {
            set(
                    backSlot() - 1,
                    Button.of(
                            Material.ARROW,
                            Text.translatable("rune.menu.previous", "Previous page")
                                    .color(NamedTextColor.YELLOW),
                            player -> setPage(page - 1)));
        }
        if (page < pageCount() - 1) {
            set(
                    backSlot() + 1,
                    Button.of(
                            Material.ARROW,
                            Text.translatable("rune.menu.next", "Next page").color(NamedTextColor.YELLOW),
                            player -> setPage(page + 1)));
        }
    }
}
