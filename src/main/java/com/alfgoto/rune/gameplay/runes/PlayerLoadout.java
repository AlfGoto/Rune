package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.gameplay.effects.Stacking;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.storage.PlayerStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The Loadout of a player: the Runes installed on them. There are {@link #SLOTS} Player Rune slots, open
 * from the start, and only passive Runes (a Rune that fits the player) can go in. The total Capacity Cost
 * cannot pass the Player Capacity of the player's Account Level.
 *
 * <p>The Runes are stored under {@code rune/loadout} in the {@link PlayerStorage} as {@code id:rank;id:rank},
 * so they belong to the player, not to an item: dying does not touch them. Slots are positions from 1;
 * uninstalling moves the next Runes up. Installing and uninstalling are free and possible anytime.
 *
 * <p>If the Account Level goes down, the installed Runes stay, even if they now cost more than the
 * Capacity: only new installs are checked.
 */
public final class PlayerLoadout {

    /** Player Rune slots, all open from the start. */
    public static final int SLOTS = 8;

    private static final String KEY = "rune/loadout";

    private final PlayerStorage storage;
    private final RuneRegistry runes;
    private final PlayerCapacity capacity;
    private final JobProgress progress;

    public PlayerLoadout(PlayerStorage storage, RuneRegistry runes, PlayerCapacity capacity, JobProgress progress) {
        this.storage = storage;
        this.runes = runes;
        this.capacity = capacity;
        this.progress = progress;
    }

    /** The installed Runes, in slot order. */
    public List<ToolLoadout.Installed> installed(UUID player) {
        List<ToolLoadout.Installed> list = new ArrayList<>();
        String raw = storage.get(player, KEY).orElse("");
        if (raw.isEmpty()) {
            return list;
        }
        for (String entry : raw.split(";")) {
            int colon = entry.lastIndexOf(':');
            if (colon < 1) {
                continue;
            }
            Optional<Rune> rune = runes.find(entry.substring(0, colon));
            if (rune.isEmpty()) {
                continue; // a Rune that no longer exists
            }
            try {
                list.add(new ToolLoadout.Installed(
                        rune.get(), Math.max(1, Integer.parseInt(entry.substring(colon + 1)))));
            } catch (NumberFormatException e) {
                // a damaged entry is skipped
            }
        }
        return list;
    }

    /** The Capacity used by the installed Runes. */
    public long used(UUID player) {
        long sum = 0;
        for (ToolLoadout.Installed installed : installed(player)) {
            sum += runes.capacityCost(installed.rune(), installed.rank());
        }
        return sum;
    }

    /** The Player Capacity of the player now (from their Account Level). */
    public int capacity(UUID player) {
        return capacity.forAccountLevel(progress.accountLevel(player));
    }

    /** Installs {@code rune} at {@code rank}. Nothing changes unless it succeeds. */
    public ToolLoadout.Result install(UUID player, Rune rune, int rank) {
        if (!rune.fits().contains(Fit.PLAYER)) {
            return ToolLoadout.Result.DOES_NOT_FIT;
        }
        if (!rune.allowsRank(rank)) {
            return ToolLoadout.Result.BAD_RANK;
        }
        List<ToolLoadout.Installed> list = installed(player);
        if (list.size() >= SLOTS) {
            return ToolLoadout.Result.NO_FREE_SLOT;
        }
        if (used(player) + runes.capacityCost(rune, rank) > capacity(player)) {
            return ToolLoadout.Result.OVER_CAPACITY;
        }
        list.add(new ToolLoadout.Installed(rune, rank));
        write(player, list);
        return ToolLoadout.Result.INSTALLED;
    }

    /** Removes the Rune in {@code slot} (from 1) and returns it, or empty if the slot is empty. */
    public Optional<ToolLoadout.Installed> uninstall(UUID player, int slot) {
        List<ToolLoadout.Installed> list = installed(player);
        if (slot < 1 || slot > list.size()) {
            return Optional.empty();
        }
        ToolLoadout.Installed removed = list.remove(slot - 1);
        write(player, list);
        return Optional.of(removed);
    }

    /** Every installed Rune as an effect source, ready for {@link Stacking}. */
    public List<Stacking.Source> sources(UUID player) {
        List<Stacking.Source> sources = new ArrayList<>();
        for (ToolLoadout.Installed installed : installed(player)) {
            sources.add(new Stacking.Source(installed.rune().effect(), installed.rank()));
        }
        return sources;
    }

    /** The stacked value of the effect {@code effectId} from the player's Runes (see {@link Stacking#total}). */
    public double total(UUID player, String effectId) {
        return Stacking.total(effectId, sources(player));
    }

    private void write(UUID player, List<ToolLoadout.Installed> list) {
        if (list.isEmpty()) {
            storage.remove(player, KEY);
            return;
        }
        StringBuilder raw = new StringBuilder();
        for (ToolLoadout.Installed installed : list) {
            if (!raw.isEmpty()) {
                raw.append(';');
            }
            raw.append(installed.rune().id()).append(':').append(installed.rank());
        }
        storage.set(player, KEY, raw.toString());
    }
}
