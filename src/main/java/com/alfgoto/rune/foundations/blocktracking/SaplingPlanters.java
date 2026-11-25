package com.alfgoto.rune.foundations.blocktracking;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Remembers who planted each sapling, in the chunk's PersistentDataContainer (survives restarts). Needed so a
 * tree that grows over time can credit the player who planted it. Main thread only.
 */
public final class SaplingPlanters {

    private final NamespacedKey key;

    public SaplingPlanters(String namespace) {
        this.key = new NamespacedKey(namespace, "sapling_planters");
    }

    /** Remembers {@code planter} as the one who planted the sapling at {@code block}. */
    public void set(Block block, UUID planter) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        Map<Long, String> all = read(pdc);
        all.put(PdcBlockTracker.pack(block), planter.toString());
        write(pdc, all);
    }

    /** Who planted the sapling at {@code block}, if known. */
    public Optional<UUID> get(Block block) {
        String id = read(block.getChunk().getPersistentDataContainer()).get(PdcBlockTracker.pack(block));
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Forgets the planter at {@code block}. */
    public void clear(Block block) {
        PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
        Map<Long, String> all = read(pdc);
        if (all.remove(PdcBlockTracker.pack(block)) != null) {
            write(pdc, all);
        }
    }

    private Map<Long, String> read(PersistentDataContainer pdc) {
        Map<Long, String> out = new LinkedHashMap<>();
        String raw = pdc.get(key, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        for (String entry : raw.split(";")) {
            int eq = entry.indexOf('=');
            if (eq < 0) {
                continue;
            }
            try {
                out.put(Long.parseLong(entry.substring(0, eq)), entry.substring(eq + 1));
            } catch (NumberFormatException ignored) {
                // skip a damaged entry
            }
        }
        return out;
    }

    private void write(PersistentDataContainer pdc, Map<Long, String> all) {
        if (all.isEmpty()) {
            pdc.remove(key);
            return;
        }
        StringBuilder sb = new StringBuilder();
        all.forEach((pos, id) -> sb.append(sb.isEmpty() ? "" : ";").append(pos).append('=').append(id));
        pdc.set(key, PersistentDataType.STRING, sb.toString());
    }
}
