package com.alfgoto.rune.gameplay.jobs;

import com.alfgoto.rune.gameplay.rewards.Drop;
import com.alfgoto.rune.gameplay.rewards.Reward;
import com.alfgoto.rune.gameplay.rewards.RollContext;
import com.alfgoto.rune.text.Text;
import java.util.random.RandomGenerator;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/** Account XP, with no Job XP behind it (see {@link JobProgress#addAccountXpBonus}). Always available. */
public final class AccountXpReward implements Reward {

    private final JobProgress progress;
    private final double amount;

    public AccountXpReward(JobProgress progress, double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Account XP must be greater than 0, got " + amount);
        }
        this.progress = progress;
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
                return Text.translatable(
                        "rune.reward.account_xp", "+%s Account XP", Component.text(Numbers.format(amount)));
            }

            @Override
            public void give(Player player) {
                progress.addAccountXpBonus(player.getUniqueId(), amount);
            }

            @Override
            public org.bukkit.inventory.ItemStack icon() {
                return Drop.named(org.bukkit.Material.NETHER_STAR, describe());
            }
        };
    }
}
