package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Text;
import com.alfgoto.rune.gameplay.jobs.JobActionEvent;
import java.util.Objects;
import java.util.function.Function;

/**
 * The kind of objective a Quest asks for, owned by one Job: an id and a base size.
 * The Quest Tier multiplies the base size. Jobs only declare these; Tiers and drawing are shared.
 */
public final class QuestType implements Identified {

    private final String id;
    private final Job job;
    private final int baseSize;
    private final QuestObjective objective;
    private final QuestAction action;
    private final Function<JobActionEvent, String> distinct;
    private final int maxSize;

    /** A Quest Type where every Job Action of the Job counts. */
    public QuestType(String id, Job job, int baseSize) {
        this(id, job, baseSize, QuestObjective.ANY);
    }

    /** A Quest Type where only the Job Actions accepted by {@code objective} count. */
    public QuestType(String id, Job job, int baseSize, QuestObjective objective) {
        this(id, job, baseSize, objective, QuestAction.BLOCK, null, Integer.MAX_VALUE);
    }

    private QuestType(
            String id,
            Job job,
            int baseSize,
            QuestObjective objective,
            QuestAction action,
            Function<JobActionEvent, String> distinct,
            int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("Max size must be 1 or more, got " + maxSize);
        }
        this.action = Objects.requireNonNull(action, "action");
        this.distinct = distinct;
        this.maxSize = maxSize;
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Quest Type id must not be blank");
        }
        if (baseSize < 1) {
            throw new IllegalArgumentException("Base size must be 1 or more, got " + baseSize);
        }
        this.id = id;
        this.job = Objects.requireNonNull(job, "job");
        this.baseSize = baseSize;
        this.objective = Objects.requireNonNull(objective, "objective");
    }

    /** The same Quest Type, counting {@code action} instead of block breaks (felled or grown trees). */
    public QuestType counting(QuestAction action) {
        return new QuestType(id, job, baseSize, objective, action, distinct, maxSize);
    }

    /**
     * The same Quest Type, where an action only counts the first time its {@code key} is seen in this Quest
     * (a null key never counts). Used by Variety: "5 different wood types".
     */
    public QuestType distinctBy(Function<JobActionEvent, String> key) {
        return new QuestType(id, job, baseSize, objective, action, Objects.requireNonNull(key, "key"), maxSize);
    }

    /** The same Quest Type, with a target never above {@code maxSize} (e.g. only 12 wood types exist). */
    public QuestType cappedAt(int maxSize) {
        return new QuestType(id, job, baseSize, objective, action, distinct, maxSize);
    }

    /** Which kind of action this Quest Type counts. */
    public QuestAction action() {
        return action;
    }

    /** The key that must be new for an action to count, or null when every matching action counts. */
    public Function<JobActionEvent, String> distinct() {
        return distinct;
    }

    /** The largest target this Quest Type can have, whatever the Tier. */
    public int maxSize() {
        return maxSize;
    }

    /** Tells which Job Actions of this Quest's Job count as progress. */
    public QuestObjective objective() {
        return objective;
    }

    @Override
    public String id() {
        return id;
    }

    public Job job() {
        return job;
    }

    public int baseSize() {
        return baseSize;
    }

    /** Translation key of the objective name, e.g. {@code rune.quest.test}. */
    public String translationKey() {
        return Text.key("quest", id);
    }

    /** English name when no language file has the key: the id with a capital. */
    public String fallbackName() {
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}
