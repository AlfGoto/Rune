package com.alfgoto.rune.visuals.unlocktree;

import com.alfgoto.rune.gameplay.items.RuneItem;
import java.util.Objects;
import net.kyori.adventure.text.Component;

/**
 * One thing a Job Level unlocks, as the Unlock Tree shows it.
 *
 * @param kind recipe, Milestone Cache or Blueprint
 * @param name its name, translatable
 * @param icon the item the node icon shows
 */
public record Unlock(Kind kind, Component name, RuneItem icon) {

    /** The kinds of unlock, from the weakest to the strongest frame (task, goal, challenge). */
    public enum Kind {
        RECIPE("task", "rune.unlock_tree.recipe", "Recipe: %s"),
        CACHE("goal", "rune.unlock_tree.cache", "Milestone Cache: %s"),
        BLUEPRINT("challenge", "rune.unlock_tree.blueprint", "Blueprint: %s");

        private final String frame;
        private final String lineKey;
        private final String lineFallback;

        Kind(String frame, String lineKey, String lineFallback) {
            this.frame = frame;
            this.lineKey = lineKey;
            this.lineFallback = lineFallback;
        }

        /** The vanilla advancement frame: task = recipe, goal = Milestone Cache, challenge = Blueprint. */
        public String frame() {
            return frame;
        }

        public String lineKey() {
            return lineKey;
        }

        public String lineFallback() {
            return lineFallback;
        }
    }

    public Unlock {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(icon, "icon");
    }
}
