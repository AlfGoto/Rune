package com.alfgoto.rune.gameplay.items;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.Reward;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** An item by id, {@code min} to {@code max} of it. Not available if the item is not registered. */
public final class ItemReward implements Reward {

    private final ItemRegistry items;
    private final String itemId;
    private final int min;
    private final int max;

    public ItemReward(ItemRegistry items, String itemId, int min, int max) {
        if (min < 1 || max < min) {
            throw new IllegalArgumentException("Count must be 1 or more, and max at least min: " + min + ".." + max);
        }
        this.items = items;
        this.itemId = itemId;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean available(RollContext context) {
        return items.find(itemId).isPresent();
    }

    @Override
    public Drop roll(RollContext context, RandomGenerator random) {
        return new ItemDrop(items.get(itemId), min + random.nextInt(max - min + 1));
    }

    private final class ItemDrop implements Drop {
        private final RuneItem item;
        private final int count;

        private ItemDrop(RuneItem item, int count) {
            this.item = item;
            this.count = count;
        }

        @Override
        public Component describe() {
            return Text.translatable(
                    "rune.reward.item",
                    "%s x%s",
                    Component.translatable(item.translationKey(), item.fallbackName())
                            .color(item.rarity().color()),
                    Component.text(count));
        }

        @Override
        public Rarity rarity() {
            return item.rarity();
        }

        @Override
        public ItemStack icon() {
            return items.create(item, count);
        }

        @Override
        public void give(Player player) {
            GiveSubcommand.give(player, items.create(item, count));
        }
    }
}
