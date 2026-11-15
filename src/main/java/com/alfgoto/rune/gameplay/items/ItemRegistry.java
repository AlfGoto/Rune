package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.config.ToolConfig;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.text.Text;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * All known {@link RuneItem}s. Shared as a service; content modules fill it (ADR 0004).
 * It builds item stacks and reads the id back from them. The id lives in the item's
 * PersistentDataContainer, so it survives restarts. The look (name, lore) is drawn by the
 * {@link ItemRedrawer}s that Visuals register.
 */
public final class ItemRegistry {

    private final Registry<RuneItem> items = new Registry<>("items");
    private final NamespacedKey idKey;
    private final List<ItemRedrawer> redrawers = new ArrayList<>();

    private final ToolConfig toolConfig;

    public ItemRegistry(Plugin plugin) {
        this(plugin, ToolConfig.defaults());
    }

    public ItemRegistry(Plugin plugin, ToolConfig toolConfig) {
        this.idKey = new NamespacedKey(plugin, "item_id");
        this.toolConfig = toolConfig;
    }

    /** The final stats of {@code item} when it is a Rune Tool, or empty. */
    public Optional<ToolStats> toolStats(RuneItem item) {
        return ToolStats.of(item, toolConfig);
    }

    public RuneItem register(RuneItem item) {
        return items.register(item);
    }

    public Optional<RuneItem> find(String id) {
        return items.find(id);
    }

    public RuneItem get(String id) {
        return items.get(id);
    }

    /** All items, in registration order. Read it when you use it, not at enable time. */
    public Collection<RuneItem> all() {
        return items.all();
    }

    /** Adds a redrawer (name, lore). It runs on every {@link #create}, {@link #update} and {@link #redraw}. */
    public void addRedrawer(ItemRedrawer redrawer) {
        redrawers.add(redrawer);
    }

    /** A new stack of {@code item}: its id and model are set, then it is drawn. */
    public ItemStack create(RuneItem item, int amount) {
        ItemStack stack = new ItemStack(item.material(), amount);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, item.id());
        meta.setItemModel(new NamespacedKey(Text.NAMESPACE, item.model()));
        stack.setItemMeta(meta);
        toolStats(item).ifPresent(stats -> ToolComponents.apply(stack, stats));
        redraw(stack);
        return stack;
    }

    /** Changes the data of {@code stack} (its meta), then redraws it. Does nothing for a vanilla stack. */
    public void update(ItemStack stack, Consumer<ItemMeta> change) {
        if (of(stack).isEmpty()) {
            return;
        }
        stack.editMeta(change);
        redraw(stack);
    }

    /** Draws the name and lore of {@code stack} again from its data. Does nothing for a vanilla stack. */
    public void redraw(ItemStack stack) {
        of(stack).ifPresent(item -> redrawers.forEach(r -> r.redraw(stack, item)));
    }

    /** The {@link RuneItem} this stack is, or empty for a vanilla stack or an unknown id. */
    public Optional<RuneItem> of(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        String id = stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        return id == null ? Optional.empty() : items.find(id);
    }
}
