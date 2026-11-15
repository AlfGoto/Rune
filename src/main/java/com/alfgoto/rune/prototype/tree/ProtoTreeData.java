package com.alfgoto.rune.prototype.tree;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

/**
 * THROWAWAY (issue #138). Hardcoded prototype data and the advancement JSON for it. Delete this
 * package once the Unlock Tree approach is decided.
 */
final class ProtoTreeData {

    static final String CRITERION = "unlock";
    static final String ROOT = "root";

    record Node(String title, String description, Material icon, String frame) {}

    record Job(String id, String name, Material icon, List<Node> nodes) {}

    static final List<Job> JOBS = List.of(
            new Job("mining", "Mining", Material.IRON_PICKAXE, List.of(
                    new Node("Mining 5", "Prototype recipe", Material.STONE_PICKAXE, "task"),
                    new Node("Mining 10", "Prototype recipe", Material.IRON_PICKAXE, "task"),
                    new Node("Mining 15", "Prototype Milestone Cache", Material.CHEST, "goal"),
                    new Node("Mining 20", "Prototype Blueprint", Material.DIAMOND_PICKAXE, "challenge"))),
            new Job("woodcutting", "Woodcutting", Material.IRON_AXE, List.of(
                    new Node("Woodcutting 5", "Prototype recipe", Material.STONE_AXE, "task"),
                    new Node("Woodcutting 10", "Prototype recipe", Material.IRON_AXE, "task"),
                    new Node("Woodcutting 15", "Prototype Milestone Cache", Material.CHEST, "goal"),
                    new Node("Woodcutting 20", "Prototype Blueprint", Material.DIAMOND_AXE, "challenge"))));

    private static final Gson GSON = new Gson();

    private ProtoTreeData() {}

    static Job job(String id) {
        return JOBS.stream().filter(job -> job.id().equals(id)).findFirst().orElse(null);
    }

    /** Path of a Job's first node (the Job icon on the left of its line). */
    static String head(Job job) {
        return job.id();
    }

    /** Path of node {@code n} (1-based) of a Job. */
    static String node(Job job, int n) {
        return job.id() + "_" + n;
    }

    /**
     * Every advancement of the tab, path → JSON, parents before children. One root, then per Job a
     * chain: root → Job head → node 1 → node 2 → ...
     */
    static Map<String, String> build(String tabTitle) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put(ROOT, json(null, display(Material.BOOKSHELF, tabTitle, "Unlock Tree prototype", "task", false,
                "minecraft:gui/advancements/backgrounds/stone")));
        for (Job job : JOBS) {
            out.put(head(job), json(ROOT, display(job.icon(), job.name(), "Job line", "task", false, null)));
            String parent = head(job);
            for (int i = 1; i <= job.nodes().size(); i++) {
                Node node = job.nodes().get(i - 1);
                out.put(node(job, i), json(parent, display(node.icon(), node.title(), node.description(),
                        node.frame(), true, null)));
                parent = node(job, i);
            }
        }
        return out;
    }

    /** The JSON with the parent key prefixed by the namespace the caller will use. */
    static String withNamespace(String json, String namespace) {
        JsonObject object = GSON.fromJson(json, JsonObject.class);
        if (object.has("parent")) {
            object.addProperty("parent", namespace + ":" + object.get("parent").getAsString());
        }
        return GSON.toJson(object);
    }

    private static String json(String parentPath, JsonObject display) {
        JsonObject root = new JsonObject();
        root.add("display", display);
        if (parentPath != null) {
            root.addProperty("parent", parentPath);
        }
        JsonObject trigger = new JsonObject();
        trigger.addProperty("trigger", "minecraft:impossible");
        JsonObject criteria = new JsonObject();
        criteria.add(CRITERION, trigger);
        root.add("criteria", criteria);
        return GSON.toJson(root);
    }

    private static JsonObject display(
            Material icon, String title, String description, String frame, boolean toast, String background) {
        JsonObject display = new JsonObject();
        JsonObject iconJson = new JsonObject();
        iconJson.addProperty("id", icon.getKey().toString());
        display.add("icon", iconJson);
        display.add("title", text(title));
        display.add("description", text(description));
        display.addProperty("frame", frame);
        display.addProperty("show_toast", toast);
        display.addProperty("announce_to_chat", false);
        if (background != null) {
            display.addProperty("background", background);
        }
        return display;
    }

    private static JsonObject text(String value) {
        JsonObject text = new JsonObject();
        text.addProperty("text", value);
        return text;
    }
}
