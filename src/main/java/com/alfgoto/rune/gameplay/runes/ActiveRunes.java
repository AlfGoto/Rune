package com.alfgoto.rune.gameplay.runes;

import com.alfgoto.rune.foundations.triggers.ClickType;
import com.alfgoto.rune.foundations.triggers.RuneClickEvent;
import com.alfgoto.rune.gameplay.effects.EffectBinding;
import com.alfgoto.rune.gameplay.items.ToolKind;
import com.alfgoto.rune.gameplay.items.ToolStats;
import com.alfgoto.rune.gameplay.runes.ToolLoadout.Installed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Runs Active Runes. A click in air with a Rune Tool triggers the Rune in the matching Active Slot (see
 * {@link ToolKind#activeClicks()}): the first Rune slots of the Loadout, one per click. A click on a block never
 * reaches here, so it stays vanilla. A Rune in any other slot, or a passive Rune in an Active Slot, does nothing.
 *
 * <p>The cooldown is per player and per Rune, so moving the Rune to another tool does not skip it. It is kept
 * in memory: it resets when the server restarts or the player leaves. It starts only when the Effect ran (all
 * its Conditions passed).
 */
public final class ActiveRunes implements Listener {

    private final ToolLoadout loadout;
    private final Predicate<ItemStack> broken;
    private final LongSupplier millis;
    private final Map<UUID, Map<String, Long>> readyAt = new HashMap<>();
    private final List<ActiveRuneListener> listeners = new CopyOnWriteArrayList<>();

    /** {@code broken} tells whether a tool is Broken: a Broken tool never triggers its Active Runes. */
    public ActiveRunes(ToolLoadout loadout, Predicate<ItemStack> broken) {
        this(loadout, broken, System::currentTimeMillis);
    }

    /** {@code millis} is the clock, so tests can move time. */
    ActiveRunes(ToolLoadout loadout, Predicate<ItemStack> broken, LongSupplier millis) {
        this.loadout = loadout;
        this.broken = broken;
        this.millis = millis;
    }

    public void addListener(ActiveRuneListener listener) {
        listeners.add(listener);
    }

    /** Seconds left before {@code rune} can be used again by {@code player}, or 0 when it is ready. */
    public double secondsLeft(UUID player, Rune rune) {
        Map<String, Long> cooldowns = readyAt.get(player);
        Long ready = cooldowns == null ? null : cooldowns.get(rune.id());
        if (ready == null) {
            return 0;
        }
        long left = ready - millis.getAsLong();
        if (left <= 0) {
            cooldowns.remove(rune.id());
            return 0;
        }
        return left / 1000.0;
    }

    /** The Rune that {@code click} triggers on {@code tool}: the one in the matching Active Slot, if any. */
    public Optional<Installed> inActiveSlot(ItemStack tool, ClickType click) {
        Optional<ToolStats> stats = loadout.stats(tool);
        if (stats.isEmpty()) {
            return Optional.empty();
        }
        int slot = stats.get().kind().activeClicks().indexOf(click);
        List<Installed> installed = loadout.installed(tool);
        if (slot < 0 || slot >= installed.size() || !installed.get(slot).rune().isActive()) {
            return Optional.empty();
        }
        return Optional.of(installed.get(slot));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    @SuppressWarnings("unchecked")
    public void onClick(RuneClickEvent event) {
        if (broken.test(event.item())) {
            return;
        }
        Optional<Installed> found = inActiveSlot(event.item(), event.click());
        if (found.isEmpty()) {
            return;
        }
        Rune rune = found.get().rune();
        int rank = found.get().rank();
        double left = secondsLeft(event.player().getUniqueId(), rune);
        if (left > 0) {
            listeners.forEach(l -> l.onCooldown(event.player(), rune, left));
            return;
        }
        // An Active Rune always has a click Effect: Rune checks it when it is built.
        EffectBinding<RuneClickEvent> binding = (EffectBinding<RuneClickEvent>) rune.effect();
        if (!binding.run(event, rank)) {
            return;
        }
        double seconds = rune.cooldownSeconds(rank);
        if (seconds > 0) {
            readyAt.computeIfAbsent(event.player().getUniqueId(), id -> new HashMap<>())
                    .put(rune.id(), millis.getAsLong() + Math.round(seconds * 1000));
        }
        listeners.forEach(l -> l.onUsed(event.player(), rune, seconds));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        readyAt.remove(event.getPlayer().getUniqueId());
    }
}
