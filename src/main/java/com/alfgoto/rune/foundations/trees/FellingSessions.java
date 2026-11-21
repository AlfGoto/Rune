package com.alfgoto.rune.foundations.trees;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.LongSupplier;
import org.bukkit.block.Block;

/**
 * In-memory felling sessions (see {@code docs/design-decisions.md}). The first natural log broken from a tree
 * that is not in a session scans the whole connected trunk (still intact at that point) and stores the log
 * positions. Each break removes its position and prunes positions that are no longer natural logs (fire,
 * explosions, other players). When none are left, the tree is felled.
 *
 * <p>Sessions are not saved: they are lost on restart, and expire after {@link #EXPIRY_MILLIS}. A tree is
 * scanned up to {@link #MAX_LOGS} logs. Main thread only.
 */
public final class FellingSessions {

    static final int MAX_LOGS = 200;
    static final long EXPIRY_MILLIS = 30L * 60L * 1000L;

    private static final class Session {
        final String wood;
        final long startedAt;
        final Set<Block> logs;
        final List<Block> bases;

        Session(String wood, long startedAt, Set<Block> logs) {
            this.wood = wood;
            this.startedAt = startedAt;
            this.logs = logs;
            // Where the trunk stood: every log of the lowest layer (a 2x2 dark oak has four).
            int lowest = logs.stream().mapToInt(Block::getY).min().orElse(0);
            this.bases = logs.stream().filter(log -> log.getY() == lowest).toList();
        }
    }

    private final TreeScan scan;
    private final LongSupplier clock;
    private final Set<Session> sessions = new HashSet<>();
    private final Map<Block, Session> byLog = new HashMap<>();
    private List<Block> lastBases = List.of();

    public FellingSessions(TreeScan scan, LongSupplier clock) {
        this.scan = scan;
        this.clock = clock;
    }

    /**
     * Call while the broken block is still in the world.
     *
     * @return true when this break took the last natural log of its tree
     */
    public boolean onLogBroken(Block block) {
        String wood = TreeScan.woodType(block.getType());
        if (wood == null || !scan.isNaturalLog(block, wood)) {
            return false;
        }
        long now = clock.getAsLong();
        expire(now);
        Session session = byLog.get(block);
        if (session == null) {
            Set<Block> logs = new HashSet<>(scan.scan(block, MAX_LOGS - 1));
            logs.add(block);
            session = new Session(wood, now, logs);
            sessions.add(session);
            for (Block log : logs) {
                byLog.put(log, session);
            }
        }
        remove(session, block);
        Iterator<Block> it = session.logs.iterator();
        while (it.hasNext()) {
            Block log = it.next();
            if (!scan.isNaturalLog(log, session.wood)) {
                it.remove();
                byLog.remove(log);
            }
        }
        if (session.logs.isEmpty()) {
            sessions.remove(session);
            lastBases = session.bases;
            return true;
        }
        return false;
    }

    /**
     * The lowest logs of the tree that the last {@link #onLogBroken} call felled (where it stood). Only
     * meaningful right after that call returned true.
     */
    public List<Block> lastBases() {
        return lastBases;
    }

    /** Number of live sessions. */
    int size() {
        return sessions.size();
    }

    private void remove(Session session, Block log) {
        session.logs.remove(log);
        byLog.remove(log);
    }

    private void expire(long now) {
        Iterator<Session> it = sessions.iterator();
        while (it.hasNext()) {
            Session session = it.next();
            if (now - session.startedAt > EXPIRY_MILLIS) {
                for (Block log : session.logs) {
                    byLog.remove(log);
                }
                it.remove();
            }
        }
    }
}
