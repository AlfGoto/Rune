package com.alfgoto.rune.visuals.unlocktree;

import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Text;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * The advancement JSON of the Unlock Tree. Kept apart from the loading ({@link AdvancementBackend}) so the
 * loading step can change (a generated datapack) without touching this.
 *
 * <p>Layout: a root, one child per Job (the start of its line), then a chain of nodes, one per Job Level
 * that unlocks something. The client draws the Job children under each other, so the lines are parallel.
 */
final class TreeJson {

    /** The only criterion of every node. It can never happen by itself: only Rune awards it. */
    static final String CRITERION = "unlocked";

    private static final String BACKGROUND = "minecraft:gui/advancements/backgrounds/stone";

    /** Job icon: vanilla material + Rune item model. Jobs not listed show paper. */
    private static final Map<String, String[]> JOB_ICONS = Map.of(
            "mining", new String[] {"wooden_pickaxe", "flint_pickaxe"},
            "woodcutting", new String[] {"wooden_axe", "flint_hatchet"});

    private TreeJson() {}

    /** The id (without namespace) of the root. */
    static String rootPath() {
        return "unlock/root";
    }

    /** The id (without namespace) of the start of a Job's line. */
    static String jobPath(Job job) {
        return "unlock/" + job.id();
    }

    /** The id (without namespace) of a node. */
    static String nodePath(UnlockNode node) {
        return "unlock/" + node.path();
    }

    static String root() {
        JsonObject display = display(
                icon("book", null),
                Text.translatable("rune.unlock_tree.title", "Unlock Tree"),
                Text.translatable("rune.unlock_tree.description", "What your Job Levels unlock."),
                "task",
                false);
        display.addProperty("background", BACKGROUND);
        return advancement(display, null);
    }

    static String job(Job job) {
        String[] icon = JOB_ICONS.getOrDefault(job.id(), new String[] {"paper", null});
        JsonObject display = display(
                icon(icon[0], icon[1]),
                Component.translatable(job.translationKey(), job.fallbackName()),
                Text.translatable(
                        "rune.unlock_tree.job",
                        "Everything %s unlocks.",
                        Component.translatable(job.translationKey(), job.fallbackName())),
                "task",
                false);
        return advancement(display, Text.NAMESPACE + ":" + rootPath());
    }

    /** A node. {@code parentPath} is the id (without namespace) of the node or Job before it. */
    static String node(UnlockNode node, String parentPath) {
        Unlock main = node.main();
        List<Component> lines = new ArrayList<>();
        for (Unlock unlock : node.unlocks()) {
            lines.add(Text.translatable(
                    unlock.kind().lineKey(),
                    unlock.kind().lineFallback(),
                    unlock.name()));
        }
        JsonObject display = display(
                icon(main.icon()),
                Text.translatable(
                        "rune.unlock_tree.node",
                        "%s %s",
                        Component.translatable(node.job().translationKey(), node.job().fallbackName()),
                        Component.text(node.level())),
                Component.join(JoinConfiguration.newlines(), lines),
                main.kind().frame(),
                true);
        return advancement(display, Text.NAMESPACE + ":" + parentPath);
    }

    private static JsonObject icon(RuneItem item) {
        return icon(item.material().getKey().getKey(), item.model());
    }

    private static JsonObject icon(String material, String model) {
        JsonObject icon = new JsonObject();
        icon.addProperty("id", "minecraft:" + material);
        if (model != null) {
            JsonObject components = new JsonObject();
            components.addProperty("minecraft:item_model", Text.NAMESPACE + ":" + model);
            icon.add("components", components);
        }
        return icon;
    }

    private static JsonObject display(
            JsonObject icon, Component title, Component description, String frame, boolean toast) {
        JsonObject display = new JsonObject();
        display.add("icon", icon);
        display.add("title", json(title));
        display.add("description", json(description));
        display.addProperty("frame", frame);
        display.addProperty("show_toast", toast);
        display.addProperty("announce_to_chat", false);
        display.addProperty("hidden", false);
        return display;
    }

    private static com.google.gson.JsonElement json(Component component) {
        return JsonParser.parseString(GsonComponentSerializer.gson().serialize(component));
    }

    private static String advancement(JsonObject display, String parent) {
        JsonObject root = new JsonObject();
        root.add("display", display);
        if (parent != null) {
            root.addProperty("parent", parent);
        }
        JsonObject criterion = new JsonObject();
        criterion.addProperty("trigger", "minecraft:impossible");
        JsonObject criteria = new JsonObject();
        criteria.add(CRITERION, criterion);
        root.add("criteria", criteria);
        return root.toString();
    }
}
