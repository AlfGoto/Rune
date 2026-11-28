package com.alfgoto.rune.visuals.codex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.visuals.menu.Menu;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class GearLoadoutTest {

    private static final int SLOT = 0;

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private ItemRegistry items;
    private ToolLoadout loadout;
    private RuneRegistry runes;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        items = plugin.context().get(ItemRegistry.class);
        loadout = plugin.context().get(ToolLoadout.class);
        runes = plugin.context().get(RuneRegistry.class);
        player.getInventory().setItem(SLOT, items.create(items.get("test_pickaxe"), 1));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private InventoryClickEvent click(int slot, ClickType type) {
        InventoryClickEvent event =
                new InventoryClickEvent(player.getOpenInventory(), SlotType.QUICKBAR, slot, type, InventoryAction.PICKUP_HALF);
        server.getPluginManager().callEvent(event);
        return event;
    }

    private boolean gearMenuOpen() {
        return player.getOpenInventory().getTopInventory().getHolder() instanceof GearMenu;
    }

    private GearMenu openGear() {
        click(SLOT, ClickType.RIGHT);
        server.getScheduler().performOneTick();
        return assertInstanceOf(GearMenu.class, player.getOpenInventory().getTopInventory().getHolder());
    }

    private void giveRune() {
        player.getInventory().addItem(runes.create(runes.get("test_rune"), 1));
    }

    private int runeItems() {
        int total = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == Material.PAPER && runes.of(stack).isPresent()) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    @Test
    void rightClickOpensTheLoadoutAndCancelsTheClick() {
        InventoryClickEvent event = click(SLOT, ClickType.RIGHT);
        assertTrue(event.isCancelled());
        assertFalse(gearMenuOpen()); // one tick later
        server.getScheduler().performOneTick();
        assertTrue(gearMenuOpen());
        assertEquals(Material.WOODEN_PICKAXE, player.getInventory().getItem(SLOT).getType());
    }

    @Test
    void leftClickShiftClickAndNumberKeyDoNothing() {
        for (ClickType type : new ClickType[] {ClickType.LEFT, ClickType.SHIFT_RIGHT, ClickType.NUMBER_KEY}) {
            InventoryClickEvent event = click(SLOT, type);
            assertFalse(event.isCancelled(), type.name());
        }
        server.getScheduler().performOneTick();
        assertFalse(gearMenuOpen());
    }

    @Test
    void aCursorWithAnItemDoesNothing() {
        player.setItemOnCursor(new ItemStack(Material.DIAMOND));
        InventoryClickEvent event = click(SLOT, ClickType.RIGHT);
        server.getScheduler().performOneTick();
        assertFalse(event.isCancelled());
        assertFalse(gearMenuOpen());
    }

    @Test
    void woodenToolsAndOtherItemsDoNothing() {
        player.getInventory().setItem(1, new ItemStack(Material.WOODEN_PICKAXE));
        player.getInventory().setItem(2, new ItemStack(Material.DIAMOND_PICKAXE));
        for (int slot : new int[] {1, 2}) {
            assertFalse(click(slot, ClickType.RIGHT).isCancelled());
        }
        server.getScheduler().performOneTick();
        assertFalse(gearMenuOpen());
    }

    @Test
    void worksOnWornArmor() {
        player.getInventory().setHelmet(items.create(items.get("test_helmet"), 1));
        click(39, ClickType.RIGHT);
        server.getScheduler().performOneTick();
        assertTrue(gearMenuOpen());
    }

    @Test
    void clickingARuneInstallsItOnTheItemAndClickingItAgainRemovesIt() {
        giveRune();
        GearMenu menu = openGear();
        assertEquals(1, loadout.slots(player.getInventory().getItem(SLOT)));

        InventoryClickEvent install = new InventoryClickEvent(
                player.getOpenInventory(), SlotType.CONTAINER, LoadoutMenu.INVENTORY_FIRST_SLOT, ClickType.LEFT,
                InventoryAction.PICKUP_ALL);
        server.getPluginManager().callEvent(install);

        assertEquals(1, loadout.installed(player.getInventory().getItem(SLOT)).size());
        assertEquals(0, runeItems());

        InventoryClickEvent remove = new InventoryClickEvent(
                player.getOpenInventory(), SlotType.CONTAINER, LoadoutMenu.SOCKETS[0], ClickType.LEFT,
                InventoryAction.PICKUP_ALL);
        server.getPluginManager().callEvent(remove);

        assertTrue(loadout.installed(player.getInventory().getItem(SLOT)).isEmpty());
        assertEquals(1, runeItems());
        assertEquals(menu, player.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    void theMenuClosesWhenTheItemLeavesTheSlot() {
        giveRune();
        GearMenu menu = openGear();
        player.getInventory().setItem(SLOT, null);

        server.getPluginManager()
                .callEvent(new InventoryClickEvent(
                        player.getOpenInventory(), SlotType.CONTAINER, LoadoutMenu.INVENTORY_FIRST_SLOT,
                        ClickType.LEFT, InventoryAction.PICKUP_ALL));

        assertFalse(player.getOpenInventory().getTopInventory().getHolder() instanceof Menu);
        assertEquals(1, runeItems()); // nothing was consumed
        assertInstanceOf(Menu.class, menu);
    }
}
