package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.Reward;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Text;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/** Reroll Tokens, capped at the most a player can hold. Always available. */
public final class RerollTokenReward implements Reward {

    private final RerollTokens tokens;
    private final int amount;

    public RerollTokenReward(RerollTokens tokens, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must be 1 or more, got " + amount);
        }
        this.tokens = tokens;
        this.amount = amount;
    }

    @Override
    public boolean available(RollContext context) {
        return true;
    }

    @Override
    public Drop roll(RollContext context, RandomGenerator random) {
        return new Drop() {
            @Override
            public Component describe() {
                return Text.translatable("rune.reward.reroll_token", "+%s Reroll Token(s)", Component.text(amount));
            }

            @Override
            public void give(Player player) {
                tokens.grant(player.getUniqueId(), amount);
            }
        };
    }
}
