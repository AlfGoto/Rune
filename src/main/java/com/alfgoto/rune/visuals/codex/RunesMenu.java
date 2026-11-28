package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.gameplay.runes.Fit;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.Rune;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.ToolLoadout;
import com.alfgoto.rune.visuals.menu.Menu;
import com.alfgoto.rune.visuals.menu.MenuStrings;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * The Runes page of the Rune Codex: the {@link LoadoutMenu} of the player, with the 8 Player Rune sockets and
 * the Player Runes found in the inventory. It only calls {@link PlayerLoadout}, so the Capacity and slot rules
 * are the ones of the Loadout.
 */
final class RunesMenu extends LoadoutMenu {

    private final PlayerLoadout loadout;
    private final UUID id;

    RunesMenu(Player player, Menu parent, PlayerLoadout loadout, RuneRegistry runes) {
        super(player, parent, runes);
        this.loadout = loadout;
        this.id = player.getUniqueId();
        draw();
    }

    @Override
    protected List<ToolLoadout.Installed> installed() {
        return loadout.installed(id);
    }

    @Override
    protected long used() {
        return loadout.used(id);
    }

    @Override
    protected int capacity() {
        return loadout.capacity(id);
    }

    @Override
    protected int slotCount() {
        return PlayerLoadout.SLOTS;
    }

    @Override
    protected ToolLoadout.Result installRune(Rune rune, int rank) {
        return loadout.install(id, rune, rank);
    }

    @Override
    protected Optional<ToolLoadout.Installed> uninstallRune(int slot) {
        return loadout.uninstall(id, slot);
    }

    @Override
    protected boolean fits(Rune rune) {
        return rune.fits().contains(Fit.PLAYER);
    }

    @Override
    protected String title(MenuStrings text) {
        return text.get("rune.menu.runes.title", "Runes");
    }

    @Override
    protected Component noSlotMessage() {
        return error("rune.menu.runes.no_slot", "All %s Player Rune slots are used.",
                Component.text(PlayerLoadout.SLOTS));
    }

    @Override
    protected Component cannotMessage() {
        return error("rune.menu.runes.cannot", "This Rune can't be installed on the player.");
    }
}
