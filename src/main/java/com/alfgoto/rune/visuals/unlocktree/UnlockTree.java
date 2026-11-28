package com.alfgoto.rune.visuals.unlocktree;

import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Text;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

/**
 * The Unlock Tree: loads the advancement tab and keeps each player's nodes equal to their Job Levels.
 * Display only: Job Level is the source of truth, vanilla's saved progress never decides anything.
 */
public final class UnlockTree {

    private static final Logger LOGGER = Logger.getLogger(UnlockTree.class.getName());

    private final AdvancementBackend backend;
    private final Supplier<Map<Job, List<UnlockNode>>> source;
    private final ToIntBiFunction<UUID, Job> jobLevel;

    private Map<Job, List<UnlockNode>> lines = Map.of();
    private boolean loaded;
    private boolean failed;

    UnlockTree(
            AdvancementBackend backend,
            Supplier<Map<Job, List<UnlockNode>>> source,
            ToIntBiFunction<UUID, Job> jobLevel) {
        this.backend = backend;
        this.source = source;
        this.jobLevel = jobLevel;
    }

    /** The lines as loaded, in Job registration order. Empty before {@link #load()}. */
    public Map<Job, List<UnlockNode>> lines() {
        return lines;
    }

    /**
     * Builds the tree from the registries as they are now and loads it. Call after every module is enabled.
     * A failure is logged once and the tree stays off: it is only a display.
     */
    public void load() {
        if (failed) {
            return;
        }
        try {
            lines = new LinkedHashMap<>(source.get());
            backend.load(key(TreeJson.rootPath()), TreeJson.root());
            for (Map.Entry<Job, List<UnlockNode>> line : lines.entrySet()) {
                backend.load(key(TreeJson.jobPath(line.getKey())), TreeJson.job(line.getKey()));
                String parent = TreeJson.jobPath(line.getKey());
                for (UnlockNode node : line.getValue()) {
                    backend.load(key(TreeJson.nodePath(node)), TreeJson.node(node, parent));
                    parent = TreeJson.nodePath(node);
                }
            }
            loaded = true;
        } catch (RuntimeException e) {
            failed = true;
            LOGGER.log(Level.WARNING, "The Unlock Tree could not be loaded; it will not be shown", e);
        }
    }

    /**
     * Makes the player's nodes match their Job Levels: the root and every Job start are always theirs, a level
     * node is theirs when the Job Level reaches it. Nodes are given and taken back, so going down works.
     *
     * @return how many nodes were given or taken back (0 when nothing changed)
     */
    public int sync(Player player) {
        if (!loaded) {
            load();
        }
        if (!loaded) {
            return 0;
        }
        int changed = 0;
        try {
            changed += set(player, TreeJson.rootPath(), true);
            for (Map.Entry<Job, List<UnlockNode>> line : lines.entrySet()) {
                changed += set(player, TreeJson.jobPath(line.getKey()), true);
                int level = jobLevel.applyAsInt(player.getUniqueId(), line.getKey());
                for (UnlockNode node : line.getValue()) {
                    changed += set(player, TreeJson.nodePath(node), level >= node.level());
                }
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "The Unlock Tree could not be synced for " + player.getName(), e);
        }
        return changed;
    }

    private int set(Player player, String path, boolean wanted) {
        NamespacedKey key = key(path);
        if (backend.isDone(player, key) == wanted) {
            return 0;
        }
        if (wanted) {
            backend.award(player, key);
        } else {
            backend.revoke(player, key);
        }
        return 1;
    }

    private static NamespacedKey key(String path) {
        return new NamespacedKey(Text.NAMESPACE, path);
    }
}
