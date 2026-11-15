package com.alfgoto.rune.visuals.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class MenuTest {

    private ServerMock server;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private InventoryClickEvent click(int slot) {
        InventoryView view = player.getOpenInventory();
        InventoryClickEvent event =
                new InventoryClickEvent(view, SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        server.getPluginManager().callEvent(event);
        return event;
    }

    private static List<Button> numbered(int count, List<Integer> clicked) {
        List<Button> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int n = i;
            items.add(Button.of(Material.PAPER, Component.text("n" + n), p -> clicked.add(n)));
        }
        return items;
    }

    @Test
    void clicksInAMenuAreCancelledAndRunTheButton() {
        List<Integer> clicked = new ArrayList<>();
        Menu menu = new Menu(Component.text("Test"), 3);
        menu.set(4, Button.of(Material.DIAMOND, Component.text("d"), p -> clicked.add(4)));
        menu.open(player);

        InventoryClickEvent event = click(4);

        assertTrue(event.isCancelled());
        assertEquals(List.of(4), clicked);
        assertEquals(Material.DIAMOND, menu.getInventory().getItem(4).getType());
    }

    @Test
    void clicksOnEmptySlotsAreCancelledToo() {
        Menu menu = new Menu(Component.text("Test"), 3);
        menu.open(player);
        assertTrue(click(10).isCancelled());
    }

    @Test
    void pagesShowTheRightButtonsAndNavigate() {
        List<Integer> clicked = new ArrayList<>();
        PagedMenu menu = new PagedMenu(Component.text("Pages"), 3, null, numbered(30, clicked));
        menu.open(player);
        assertEquals(18, menu.pageSize());
        assertEquals(2, menu.pageCount());
        assertEquals(null, menu.getInventory().getItem(21)); // no previous on page 1
        assertEquals(Material.ARROW, menu.getInventory().getItem(23).getType());

        click(21); // empty slot: nothing happens
        assertEquals(0, menu.page());
        click(23); // next
        assertEquals(1, menu.page());

        click(0);
        assertEquals(List.of(18), clicked);
        assertEquals(null, menu.getInventory().getItem(12)); // only 12 items on page 2
        assertEquals(null, menu.getInventory().getItem(23)); // no next on the last page

        click(21); // previous
        assertEquals(0, menu.page());
    }

    @Test
    void backButtonReturnsToTheParent() {
        Menu root = new Menu(Component.text("Root"), 3);
        Menu child = new Menu(Component.text("Child"), 3, root);
        child.open(player);
        assertSame(child, player.getOpenInventory().getTopInventory().getHolder());

        click(22); // size 27 - 5

        assertSame(root, player.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    void debugMenuOpens() {
        new MenuDebug().root().open(player);
        assertTrue(player.getOpenInventory().getTopInventory().getHolder() instanceof Menu);
    }
}
