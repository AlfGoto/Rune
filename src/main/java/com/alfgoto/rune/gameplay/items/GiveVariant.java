package com.alfgoto.rune.gameplay.items;

import java.util.List;
import org.bukkit.entity.Player;

/**
 * A special form of {@code /rune give}: {@code /rune give <keyword> ...}. An item type with extra data
 * (e.g. the Rank of a Rune) registers one on the {@link GiveSubcommand}.
 */
public interface GiveVariant {

    /** The word typed after {@code give}. Lower case. */
    String keyword();

    /** Runs the command. {@code args} are the words after the keyword. */
    void execute(Player player, List<String> args);

    /** Tab completions for the words after the keyword. */
    default List<String> complete(List<String> args) {
        return List.of();
    }
}
