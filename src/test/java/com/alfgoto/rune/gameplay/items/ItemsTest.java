package com.alfgoto.rune.gameplay.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.command.RuneCommand;
import com.alfgoto.rune.text.Rarity;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class ItemsTest {

    private ServerMock server;
    private RunePlugin plugin;
    private ItemRegistry registry;
    private GiveSubcommand give;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(RunePlugin.class);
        registry = new ItemRegistry(plugin);
        registry.register(new RuneItem("test_item", Rarity.COMMON, Material.PAPER, "test_rune"));
        registry.register(new RuneItem("other_item", Rarity.RARE, Material.STICK, "test_rune"));
        give = new GiveSubcommand(registry);
        player = server.addPlayer();
        player.addAttachment(plugin, RuneCommand.PERMISSION, true);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private int count(Material material) {
        return player.getInventory().all(material).values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    @Test
    void createdStackKeepsItsIdAndModel() {
        RuneItem item = registry.get("test_item");
        ItemStack stack = registry.create(item, 3);
        assertEquals(3, stack.getAmount());
        assertEquals(item, registry.of(stack).orElseThrow());
        assertEquals(new NamespacedKey("rune", "test_rune"), stack.getItemMeta().getItemModel());
    }

    @Test
    void idSurvivesSerialization() {
        ItemStack stack = registry.create(registry.get("test_item"), 1);
        ItemStack loaded = ItemStack.deserializeBytes(stack.serializeAsBytes());
        assertEquals("test_item", registry.of(loaded).orElseThrow().id());
    }

    @Test
    void vanillaStackIsNotARuneItem() {
        assertFalse(registry.of(new ItemStack(Material.PAPER)).isPresent());
        assertFalse(registry.of(null).isPresent());
    }

    @Test
    void giveAddsItemToInventory() {
        give.execute(player, List.of("test_item", "5"));
        assertEquals(5, count(Material.PAPER));
        assertTrue(registry.of(player.getInventory().getItem(0)).isPresent());
    }

    @Test
    void giveSplitsAcrossStacks() {
        give.execute(player, List.of("other_item", "100"));
        assertEquals(100, count(Material.STICK));
    }

    @Test
    void giveRejectsBadInput() {
        give.execute(player, List.of("nope"));
        give.execute(player, List.of("test_item", "0"));
        give.execute(player, List.of("test_item", "abc"));
        give.execute(player, List.of());
        assertEquals(0, count(Material.PAPER));
    }

    @Test
    void tabCompletesIds() {
        assertEquals(List.of("test_item", "other_item"), give.complete(player, List.of("")));
        assertEquals(List.of("test_item"), give.complete(player, List.of("te")));
        assertEquals(List.of(), give.complete(player, List.of("test_item", "")));
    }

    @Test
    void pluginRegistersGiveCommandAndTestItem() {
        player.performCommand("rune give test_item");
        assertEquals(1, count(Material.PAPER));
    }
}
