package com.alfgoto.rune.gameplay.blueprints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.foundations.config.BlueprintConfig;
import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.crafting.RecipeBook;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.LevelCurve;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class BlueprintsTest {

    private final Job mining = Job.builder("mining").build();
    private ServerMock server;
    private PlayerMock player;
    private JobProgress progress;
    private RecipeBook book;
    private Blueprints blueprints;
    private Blueprint blueprint;
    private CraftingRecipe recipe;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        PlayerStorage storage = new MemoryStorage();
        progress = new JobProgress(storage, new LevelCurve(50, 1.3), new LevelCurve(200, 1.3));
        ItemRegistry items = new ItemRegistry(plugin);
        items.register(new RuneItem(Blueprints.ITEM_ID, Rarity.LEGENDARY, Material.NAME_TAG, "test_rune"));
        items.register(new RuneItem("bp_result", Rarity.LEGENDARY, Material.STICK, "test_rune"));
        book = new RecipeBook(plugin, items, progress);
        recipe = book.register(CraftingRecipe.blueprint(
                "bp_recipe", mining, "bp_result", 1, List.of("SS"), Map.of('S', Ingredient.vanilla(Material.STONE))));
        blueprints = new Blueprints(plugin, items, book, storage);
        book.setBlueprintCheck(blueprints::isUnlocked);
        blueprint = blueprints.register(new Blueprint("bp_recipe"));
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private void rightClick(ItemStack stack) {
        player.getInventory().setItemInMainHand(stack);
        Block block = player.getWorld().getBlockAt(0, 64, 0);
        new BlueprintListener(blueprints)
                .onInteract(new PlayerInteractEvent(
                        player, Action.RIGHT_CLICK_BLOCK, stack, block, BlockFace.UP, EquipmentSlot.HAND));
    }

    @Test
    void recipeIsLockedWhateverTheJobLevel() {
        progress.setJobLevel(player.getUniqueId(), mining, 40);
        assertFalse(book.isUnlocked(player.getUniqueId(), recipe));
    }

    @Test
    void usingTheBlueprintUnlocksTheRecipeAndTakesOne() {
        ItemStack stack = blueprints.create(blueprint);
        stack.setAmount(2);
        rightClick(stack);
        assertTrue(book.isUnlocked(player.getUniqueId(), recipe));
        assertTrue(blueprints.isUnlocked(player.getUniqueId(), "bp_recipe"));
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void aBlueprintAlreadyUsedIsKept() {
        blueprints.unlock(player, blueprint);
        rightClick(blueprints.create(blueprint));
        assertEquals(1, player.getInventory().getItemInMainHand().getAmount());
    }

    @Test
    void unlockingTwiceSaysSo() {
        assertTrue(blueprints.unlock(player, blueprint));
        assertFalse(blueprints.unlock(player, blueprint));
    }

    @Test
    void theStackKnowsItsBlueprint() {
        ItemStack stack = blueprints.create(blueprint);
        assertEquals(Optional.of(blueprint), blueprints.of(stack));
        assertTrue(blueprints.idOf(new ItemStack(Material.PAPER)).isEmpty());
        assertTrue(blueprints.idOf(null).isEmpty());
    }

    @Test
    void aBlueprintNeedsABlueprintRecipe() {
        assertThrows(IllegalArgumentException.class, () -> blueprints.register(new Blueprint("nothing")));
    }

    @Test
    void milestoneBlueprintIsGivenWhenTheLevelIsPassed() {
        BlueprintMilestones milestones = new BlueprintMilestones(
                blueprints, new BlueprintConfig(List.of(new BlueprintConfig.Milestone("mining", 22, "bp_recipe"))));
        progress.addLevelListener(milestones);
        progress.setJobLevel(player.getUniqueId(), mining, 21);
        assertTrue(player.getInventory().isEmpty());
        progress.setJobLevel(player.getUniqueId(), mining, 22);
        assertEquals(Optional.of(blueprint), blueprints.of(player.getInventory().getItem(0)));
    }

    @Test
    void milestoneBlueprintIsGivenWhenALevelIsSkipped() {
        progress.addLevelListener(new BlueprintMilestones(
                blueprints, new BlueprintConfig(List.of(new BlueprintConfig.Milestone("mining", 22, "bp_recipe")))));
        progress.setJobLevel(player.getUniqueId(), mining, 30);
        assertEquals(Optional.of(blueprint), blueprints.of(player.getInventory().getItem(0)));
    }

    @Test
    void milestoneBlueprintIsNotGivenForAnotherJobOrAfterUse() {
        progress.addLevelListener(new BlueprintMilestones(
                blueprints,
                new BlueprintConfig(List.of(
                        new BlueprintConfig.Milestone("woodcutting", 22, "bp_recipe"),
                        new BlueprintConfig.Milestone("mining", 22, "bp_recipe")))));
        blueprints.unlock(player, blueprint);
        progress.setJobLevel(player.getUniqueId(), mining, 25);
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void milestoneOfAnUnregisteredBlueprintIsSkipped() {
        progress.addLevelListener(new BlueprintMilestones(
                blueprints, new BlueprintConfig(List.of(new BlueprintConfig.Milestone("mining", 22, "briseroc")))));
        progress.setJobLevel(player.getUniqueId(), mining, 25);
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void rewardIsAvailableUntilTheBlueprintIsUsed() {
        BlueprintReward reward = new BlueprintReward(blueprints, "bp_recipe");
        RollContext context = RollContext.of(player.getUniqueId(), Rarity.LEGENDARY);
        assertTrue(reward.available(context));
        reward.roll(context, new java.util.Random(1)).give(player);
        assertEquals(Optional.of(blueprint), blueprints.of(player.getInventory().getItem(0)));
        blueprints.unlock(player, blueprint);
        assertFalse(reward.available(context));
        assertFalse(new BlueprintReward(blueprints, "unknown").available(context));
    }

    @Test
    void configReadsMilestonesAndSkipsBadEntries() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("blueprints.milestones.mining.22", "briseroc");
        yaml.set("blueprints.milestones.mining.zero", "bad");
        yaml.set("blueprints.milestones.woodcutting.30", "elders_bark");
        BlueprintConfig config = BlueprintConfig.load(yaml, Logger.getAnonymousLogger());
        assertEquals(
                List.of(
                        new BlueprintConfig.Milestone("mining", 22, "briseroc"),
                        new BlueprintConfig.Milestone("woodcutting", 30, "elders_bark")),
                config.milestones());
    }

    @Test
    void defaultConfigHasTheDocumentedMilestones() {
        List<BlueprintConfig.Milestone> milestones = BlueprintConfig.defaults().milestones();
        assertTrue(milestones.contains(new BlueprintConfig.Milestone("mining", 22, "briseroc")));
        assertTrue(milestones.contains(new BlueprintConfig.Milestone("woodcutting", 22, "elder_axe")));
        assertTrue(milestones.contains(new BlueprintConfig.Milestone("mining", 30, "heart_of_the_mountain")));
        assertTrue(milestones.contains(new BlueprintConfig.Milestone("woodcutting", 30, "elders_bark")));
    }

    private static final class MemoryStorage implements PlayerStorage {
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
