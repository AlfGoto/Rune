package com.alfgoto.rune.gameplay.blueprints;

import com.alfgoto.rune.gameplay.items.GiveSubcommand;
import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.Reward;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Rarity;
import com.alfgoto.rune.text.Text;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * One Blueprint by id, for a loot table entry (a Quest reward, a vault). Not available when the Blueprint is not
 * registered or the player already used it. Content tickets add the entries.
 */
public final class BlueprintReward implements Reward {

    private final Blueprints blueprints;
    private final String blueprintId;

    public BlueprintReward(Blueprints blueprints, String blueprintId) {
        this.blueprints = blueprints;
        this.blueprintId = blueprintId;
    }

    @Override
    public boolean available(RollContext context) {
        return blueprints.find(blueprintId).isPresent() && !blueprints.isUnlocked(context.player(), blueprintId);
    }

    @Override
    public Drop roll(RollContext context, RandomGenerator random) {
        return new BlueprintDrop(blueprints.find(blueprintId).orElseThrow());
    }

    private final class BlueprintDrop implements Drop {
        private final Blueprint blueprint;

        private BlueprintDrop(Blueprint blueprint) {
            this.blueprint = blueprint;
        }

        @Override
        public Component describe() {
            return Text.translatable(
                            "rune.reward.blueprint",
                            "Blueprint: %s",
                            Component.translatable(blueprint.translationKey(), blueprint.fallbackName()))
                    .color(Rarity.LEGENDARY.color());
        }

        @Override
        public Rarity rarity() {
            return Rarity.LEGENDARY;
        }

        @Override
        public ItemStack icon() {
            return blueprints.create(blueprint);
        }

        @Override
        public void give(Player player) {
            GiveSubcommand.give(player, blueprints.create(blueprint));
        }
    }
}
