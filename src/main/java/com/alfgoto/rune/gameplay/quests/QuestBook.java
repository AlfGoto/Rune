package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.config.RuneConfig;
import com.alfgoto.rune.foundations.registry.Registry;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.storage.PlayerStorage;
import com.alfgoto.rune.text.Rarity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.random.RandomGenerator;

/**
 * The 3 active Quests of each player, stored in {@link PlayerStorage} as {@code <type id>:<tier id>}
 * under {@code quest/slot/1..3}. An empty slot is filled when the Quests are read, so a player always
 * has 3 as long as at least one Quest Type is eligible.
 */
public final class QuestBook {

    /** Number of active Quests. */
    public static final int SLOTS = 3;

    private final PlayerStorage storage;
    private final JobProgress progress;
    private final Registry<QuestType> types;
    private final RuneConfig config;
    private final TierDraw tierDraw;
    private final RandomGenerator random;
    private final RerollTokens rerollTokens;

    public QuestBook(
            PlayerStorage storage,
            JobProgress progress,
            Registry<QuestType> types,
            RuneConfig config,
            RandomGenerator random,
            LongSupplier worldDay) {
        this.rerollTokens = new RerollTokens(storage, config, worldDay);
        this.storage = storage;
        this.progress = progress;
        this.types = types;
        this.config = config;
        this.tierDraw = new TierDraw(config);
        this.random = random;
    }

    /** The active Quests, slot 1 first. Fills empty slots with a new Quest first. */
    public List<Quest> active(UUID player) {
        List<Quest> out = new ArrayList<>();
        for (int slot = 1; slot <= SLOTS; slot++) {
            Quest quest = read(player, slot);
            if (quest == null) {
                quest = draw(player);
                if (quest == null) {
                    continue; // nothing eligible yet
                }
                write(player, slot, quest);
            }
            out.add(quest);
        }
        return out;
    }

    /** Replaces the Quest in {@code slot} (1..3), ignoring Job Level and Tier gates. */
    public Quest set(UUID player, int slot, QuestType type, Rarity tier) {
        requireSlot(slot);
        Quest quest = quest(type, tier);
        write(player, slot, quest);
        return quest;
    }

    /** The player's Reroll Tokens. */
    public RerollTokens rerollTokens() {
        return rerollTokens;
    }

    /**
     * Spends one Reroll Token to replace the Quest in {@code slot} (1..3) with a new draw.
     * Returns empty, spending nothing, if the player has no Reroll Token or no Quest can be drawn.
     */
    public Optional<Quest> reroll(UUID player, int slot) {
        requireSlot(slot);
        if (rerollTokens.count(player) <= 0) {
            return Optional.empty();
        }
        Quest quest = draw(player);
        if (quest == null) {
            return Optional.empty();
        }
        rerollTokens.spend(player);
        write(player, slot, quest);
        return Optional.of(quest);
    }

    /** Objective size: base size × Tier multiplier, rounded up. */
    public int target(QuestType type, Rarity tier) {
        return (int) Math.ceil(type.baseSize() * config.questTierMultipliers().get(tier.ordinal()));
    }

    /** Draws a Quest: a random eligible Job, one of its Quest Types, a Tier weighted by that Job's level. */
    Quest draw(UUID player) {
        Map<Job, List<QuestType>> eligible = new LinkedHashMap<>();
        for (QuestType type : types.all()) {
            Job job = type.job();
            if (progress.jobLevel(player, job) >= job.questMinLevel()) {
                eligible.computeIfAbsent(job, j -> new ArrayList<>()).add(type);
            }
        }
        if (eligible.isEmpty()) {
            return null;
        }
        List<Job> jobs = new ArrayList<>(eligible.keySet());
        Job job = jobs.get(random.nextInt(jobs.size()));
        List<QuestType> candidates = eligible.get(job);
        QuestType type = candidates.get(random.nextInt(candidates.size()));
        Rarity tier = tierDraw.draw(progress.jobLevel(player, job), random);
        return quest(type, tier);
    }

    private Quest quest(QuestType type, Rarity tier) {
        return new Quest(type, tier, target(type, tier));
    }

    private Quest read(UUID player, int slot) {
        String raw = storage.get(player, key(slot)).orElse(null);
        if (raw == null) {
            return null;
        }
        int colon = raw.indexOf(':');
        if (colon < 0) {
            return null;
        }
        QuestType type = types.find(raw.substring(0, colon)).orElse(null);
        Rarity tier = Rarity.fromId(raw.substring(colon + 1));
        return type == null || tier == null ? null : quest(type, tier);
    }

    private void write(UUID player, int slot, Quest quest) {
        storage.set(player, key(slot), quest.type().id() + ":" + quest.tier().id());
    }

    private static String key(int slot) {
        return "quest/slot/" + slot;
    }

    private static void requireSlot(int slot) {
        if (slot < 1 || slot > SLOTS) {
            throw new IllegalArgumentException("Slot must be 1 to " + SLOTS + ", got " + slot);
        }
    }
}
