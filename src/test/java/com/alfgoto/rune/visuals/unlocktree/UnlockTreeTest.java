package com.alfgoto.rune.visuals.unlocktree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.gameplay.crafting.CraftingRecipe;
import com.alfgoto.rune.gameplay.crafting.Ingredient;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Rarity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

class UnlockTreeTest {

    /** Remembers what was loaded and what each player has. */
    private static final class FakeBackend implements AdvancementBackend {
        final Map<NamespacedKey, String> loaded = new LinkedHashMap<>();
        final Set<NamespacedKey> done = new HashSet<>();
        int awards;
        int revokes;

        @Override
        public void load(NamespacedKey key, String json) {
            loaded.put(key, json);
        }

        @Override
        public boolean isDone(Player player, NamespacedKey key) {
            return done.contains(key);
        }

        @Override
        public void award(Player player, NamespacedKey key) {
            awards++;
            done.add(key);
        }

        @Override
        public void revoke(Player player, NamespacedKey key) {
            revokes++;
            done.remove(key);
        }
    }

    private ServerMock server;
    private Player player;
    private FakeBackend backend;
    private UnlockTree tree;
    private final Map<String, Integer> levels = new HashMap<>();
    private final Job mining = Job.builder("mining").build();
    private final Job woodcutting = Job.builder("woodcutting").build();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        backend = new FakeBackend();
        RuneItem item = new RuneItem("tool", Rarity.COMMON, Material.WOODEN_PICKAXE, "tool");
        Map<Job, List<UnlockNode>> lines = new LinkedHashMap<>();
        lines.put(mining, List.of(node(mining, 2), node(mining, 12)));
        lines.put(woodcutting, List.of(node(woodcutting, 5)));
        tree = new UnlockTree(backend, () -> lines, (uuid, job) -> levels.getOrDefault(job.id(), 1));
    }

    private UnlockNode node(Job job, int level) {
        RuneItem item = new RuneItem("tool", Rarity.COMMON, Material.WOODEN_PICKAXE, "tool");
        return new UnlockNode(job, level, List.of(new Unlock(
                Unlock.Kind.RECIPE, net.kyori.adventure.text.Component.text("Tool"), item)));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static NamespacedKey key(String path) {
        return new NamespacedKey("rune", path);
    }

    @Test
    void everyJobIsOneLineUnderTheSameRoot() {
        tree.load();
        assertTrue(backend.loaded.get(key("unlock/mining")).contains("\"parent\":\"rune:unlock/root\""));
        assertTrue(backend.loaded.get(key("unlock/woodcutting")).contains("\"parent\":\"rune:unlock/root\""));
        assertFalse(backend.loaded.get(key("unlock/root")).contains("\"parent\""));
        assertTrue(backend.loaded.get(key("unlock/mining/l2")).contains("\"parent\":\"rune:unlock/mining\""));
        assertTrue(backend.loaded.get(key("unlock/mining/l12")).contains("\"parent\":\"rune:unlock/mining/l2\""));
        assertTrue(backend.loaded.get(key("unlock/woodcutting/l5")).contains("\"parent\":\"rune:unlock/woodcutting\""));
    }

    @Test
    void syncGivesTheNodesTheJobLevelReachesAndTakesThemBackWhenItGoesDown() {
        levels.put("mining", 12);
        tree.sync(player);
        assertTrue(backend.done.contains(key("unlock/mining/l2")));
        assertTrue(backend.done.contains(key("unlock/mining/l12")));
        assertFalse(backend.done.contains(key("unlock/woodcutting/l5")));

        levels.put("mining", 3);
        assertEquals(1, tree.sync(player));
        assertTrue(backend.done.contains(key("unlock/mining/l2")));
        assertFalse(backend.done.contains(key("unlock/mining/l12")));
        assertEquals(1, backend.revokes);
    }

    @Test
    void vanillaProgressNeverDecides() {
        levels.put("mining", 1);
        backend.done.add(key("unlock/mining/l12")); // saved by vanilla from an older session
        tree.sync(player);
        assertFalse(backend.done.contains(key("unlock/mining/l12")));
    }

    @Test
    void aSyncThatChangesNothingReportsZero() {
        tree.sync(player);
        assertEquals(0, tree.sync(player));
    }
}
