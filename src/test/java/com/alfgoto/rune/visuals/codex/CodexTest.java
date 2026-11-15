package com.alfgoto.rune.visuals.codex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.Codex;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.visuals.menu.Menu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class CodexTest {

    private ServerMock server;
    private RunePlugin plugin;
    private PlayerMock player;
    private Codex codex;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        player = server.addPlayer();
        codex = plugin.context().get(Codex.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private InventoryClickEvent click(int slot, ClickType type, InventoryAction action) {
        InventoryClickEvent event =
                new InventoryClickEvent(player.getOpenInventory(), SlotType.QUICKBAR, slot, type, action);
        server.getPluginManager().callEvent(event);
        return event;
    }

    @Test
    void joiningGivesTheCodexInTheLastSlot() {
        assertTrue(codex.is(player.getInventory().getItem(Codex.SLOT)));
        assertEquals(35, Codex.SLOT);
    }

    @Test
    void ensureMovesWhatWasInTheSlotAndKeepsOneCodex() {
        player.getInventory().setItem(Codex.SLOT, new ItemStack(Material.DIAMOND));
        codex.ensure(player);
        assertTrue(codex.is(player.getInventory().getItem(Codex.SLOT)));
        assertTrue(player.getInventory().contains(Material.DIAMOND));
        long codexes = java.util.Arrays.stream(player.getInventory().getContents())
                .filter(codex::is)
                .count();
        assertEquals(1, codexes);
    }

    @Test
    void shiftClickAndHotkeyOnTheCodexAreCancelled() {
        assertTrue(click(Codex.SLOT, ClickType.SHIFT_RIGHT, InventoryAction.MOVE_TO_OTHER_INVENTORY)
                .isCancelled());
        assertTrue(click(Codex.SLOT, ClickType.DROP, InventoryAction.DROP_ONE_SLOT).isCancelled());
        assertTrue(click(Codex.SLOT, ClickType.SWAP_OFFHAND, InventoryAction.HOTBAR_SWAP)
                .isCancelled());
    }

    @Test
    void droppingTheCodexIsCancelled() {
        var drop = player.getWorld().dropItem(player.getLocation(), player.getInventory().getItem(Codex.SLOT));
        PlayerDropItemEvent event = new PlayerDropItemEvent(player, drop);
        server.getPluginManager().callEvent(event);
        assertTrue(event.isCancelled());
    }

    @Test
    void theCodexIsNotInTheDeathDrops() {
        java.util.List<ItemStack> drops = new java.util.ArrayList<>();
        drops.add(player.getInventory().getItem(Codex.SLOT));
        drops.add(new ItemStack(Material.DIRT));
        PlayerDeathEvent event = new PlayerDeathEvent(player, org.bukkit.damage.DamageSource.builder(
                        org.bukkit.damage.DamageType.GENERIC)
                .build(), drops, 0, Component.empty(), false);
        server.getPluginManager().callEvent(event);
        assertEquals(1, event.getDrops().size());
        assertEquals(Material.DIRT, event.getDrops().get(0).getType());
    }

    @Test
    void clickingTheCodexOpensTheMainMenu() {
        InventoryClickEvent event = click(Codex.SLOT, ClickType.LEFT, InventoryAction.PICKUP_ALL);
        assertTrue(event.isCancelled());
        assertInstanceOf(Menu.class, player.getOpenInventory().getTopInventory().getHolder());
    }

    @Test
    void jobsPageShowsLevelsAndAccountLevelFromTheSameNumbersAsXp() {
        JobRegistry jobs = plugin.context().get(JobRegistry.class);
        JobProgress progress = plugin.context().get(JobProgress.class);
        Job job = jobs.registry().all().iterator().next();
        progress.addJobXp(player.getUniqueId(), job, 120);
        CodexMenus menus = new CodexMenus(
                jobs.registry(),
                progress,
                plugin.context().get(com.alfgoto.rune.gameplay.runes.PlayerCapacity.class));

        Menu page = menus.jobsPage(player, menus.main(player));

        ItemStack account = page.getInventory().getItem(0);
        assertNotNull(account);
        assertEquals(Material.NETHER_STAR, account.getType());
        String capacity = PlainTextComponentSerializer.plainText().serialize(account.lore().get(0));
        assertTrue(capacity.contains(String.valueOf(5 + progress.accountLevel(player.getUniqueId()))), capacity);
        ItemStack jobIcon = page.getInventory().getItem(1);
        int level = progress.jobLevel(player.getUniqueId(), job);
        String xpLine = PlainTextComponentSerializer.plainText().serialize(jobIcon.lore().get(2));
        assertTrue(xpLine.contains("120"), xpLine);
        assertTrue(PlainTextComponentSerializer.plainText()
                .serialize(jobIcon.lore().get(0))
                .contains(String.valueOf(level)));
    }

    @Test
    void barFillsWithTheShareOfTheLevel() {
        LevelCurve curve = new LevelCurve(100, 1);
        // Level 1 needs 100 XP: 50 XP is half of the bar.
        Component bar = CodexMenus.bar(50, curve, 1);
        String whole = PlainTextComponentSerializer.plainText().serialize(bar);
        assertEquals(CodexMenus.BAR_LENGTH, whole.length());
        // The root holds the filled (green) squares, its child the empty ones.
        String filled = assertInstanceOf(net.kyori.adventure.text.TextComponent.class, bar)
                .content();
        assertEquals(CodexMenus.BAR_LENGTH / 2, filled.length());
    }
}
