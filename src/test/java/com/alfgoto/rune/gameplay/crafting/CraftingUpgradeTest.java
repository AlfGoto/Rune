package com.alfgoto.rune.gameplay.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.gameplay.effects.Effect;
import com.alfgoto.rune.gameplay.effects.EffectTriggers;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolSpec;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.runes.CapacityCost;
import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

/** The Upgrade: a craft that eats Rune gear keeps the Runes that fit and gives back the rest. */
class CraftingUpgradeTest {

    private final Job mining = Job.builder("mining").build();
    private ServerMock server;
    private RunePlugin plugin;
    private ItemRegistry items;
    private RuneRegistry runes;
    private ToolLoadout loadout;
    private RecipeBook book;
    private Rune speed;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        JobProgress progress = new JobProgress(new Memory(), new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        items = new ItemRegistry(plugin, ToolConfig.defaults());
        runes = new RuneRegistry(plugin, items, new CapacityCost(RuneConfig.defaults()));
        loadout = new ToolLoadout(plugin, items, runes);
        Effect<?> effect = Effect.builder("speed", EffectTriggers.BLOCK_BREAK).does(context -> {});
        speed = runes.register(new Rune(
                new RuneItem("speed", Rarity.COMMON, Material.PAPER, "test_rune"),
                effect.with(10, 10),
                Set.of(Fit.PICKAXE)));
        items.register(RuneItem.tool("rare_pick", Rarity.RARE, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        items.register(RuneItem.tool("common_pick", Rarity.COMMON, ToolKind.PICKAXE, "test_rune", ToolSpec.DEFAULT));
        book = new RecipeBook(plugin, items, progress);
        book.register(new CraftingRecipe(
                "downgrade", mining, 1, "common_pick", 1, List.of("P"), Map.of('P', Ingredient.rune("rare_pick"))));
        player = server.addPlayer();
        server.getPluginManager().registerEvents(new CraftingListener(book, items, loadout, runes), plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private CraftItemEvent craft(ClickType click, boolean cancelLater) {
        ItemStack old = items.create(items.get("rare_pick"), 1); // 3 slots
        loadout.install(old, speed, 1);
        loadout.install(old, speed, 1); // the common result has 1 slot: one Rune is left over
        InventoryView view = player.openWorkbench(null, true);
        CraftingInventory inventory = (CraftingInventory) view.getTopInventory();
        inventory.setMatrix(new ItemStack[] {old, null, null, null, null, null, null, null, null});
        if (cancelLater) {
            server.getPluginManager().registerEvents(new Canceller(), plugin);
        }
        CraftItemEvent event = new CraftItemEvent(
                server.getRecipe(book.key("downgrade")),
                view,
                InventoryType.SlotType.RESULT,
                0,
                click,
                InventoryAction.PICKUP_ALL);
        server.getPluginManager().callEvent(event);
        return event;
    }

    private long runeItemsInInventory() {
        return java.util.Arrays.stream(player.getInventory().getContents())
                .filter(java.util.Objects::nonNull)
                .filter(stack -> stack.getType() == Material.PAPER)
                .count();
    }

    @Test
    void resultKeepsTheRunesThatFitAndTheRestGoBackToTheInventory() {
        CraftItemEvent event = craft(ClickType.LEFT, false);
        assertFalse(event.isCancelled());
        assertEquals(1, loadout.installed(event.getCurrentItem()).size());
        assertEquals(1, runeItemsInInventory());
    }

    @Test
    void aCancelledCraftGivesNoRunes() {
        CraftItemEvent event = craft(ClickType.LEFT, true);
        assertTrue(event.isCancelled());
        assertEquals(0, runeItemsInInventory());
    }

    @Test
    void shiftClickIsRefusedWhenThereAreRunesToCarryOver() {
        CraftItemEvent event = craft(ClickType.SHIFT_LEFT, false);
        assertTrue(event.isCancelled());
        assertEquals(0, runeItemsInInventory());
    }

    @Test
    void numberKeyAndDropAreRefusedWhenThereAreRunesToCarryOver() {
        for (ClickType click : new ClickType[] {ClickType.NUMBER_KEY, ClickType.DROP, ClickType.CONTROL_DROP}) {
            CraftItemEvent event = craft(click, false);
            assertTrue(event.isCancelled(), click.name());
        }
        assertEquals(0, runeItemsInInventory());
    }

    /** Another listener (like Locked Gear) that cancels the craft after Crafting looked at it. */
    private static final class Canceller implements Listener {
        @EventHandler(priority = EventPriority.HIGH)
        public void onCraft(CraftItemEvent event) {
            event.setCancelled(true);
        }
    }

    private static final class Memory implements PlayerStorage {
        private final Map<String, String> data = new HashMap<>();

        @Override
        public Optional<String> get(UUID player, String key) {
            return Optional.ofNullable(data.get(player + key));
        }

        @Override
        public void set(UUID player, String key, String value) {
            data.put(player + key, value);
        }

        @Override
        public void remove(UUID player, String key) {
            data.remove(player + key);
        }
    }
}
