package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.text.Text;
import java.util.Collection;
import java.util.Optional;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * All known {@link RuneItem}s. Shared as a service; content modules fill it (ADR 0004).
 * It builds item stacks and reads the id back from them. The id lives in the item's
 * PersistentDataContainer, so it survives restarts.
 */
public final class ItemRegistry {

    private final Registry<RuneItem> items = new Registry<>("items");
    private final NamespacedKey idKey;

    public ItemRegistry(Plugin plugin) {
        this.idKey = new NamespacedKey(plugin, "item_id");
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

    /** A new stack of {@code item}: its id, model and name are set. */
    public ItemStack create(RuneItem item, int amount) {
        ItemStack stack = new ItemStack(item.material(), amount);
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, item.id());
        meta.setItemModel(new NamespacedKey(Text.NAMESPACE, item.model()));
        meta.itemName(Text.translatable(item.translationKey(), item.fallbackName())
                .color(item.rarity().color())
                .decoration(TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
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
