package com.alfgoto.rune.gameplay.quests;

import com.alfgoto.rune.foundations.registry.Identified;
import com.alfgoto.rune.gameplay.jobs.Job;
import com.alfgoto.rune.text.Text;
import java.util.Objects;

/**
 * The kind of objective a Quest asks for, owned by one Job: an id and a base size.
 * The Quest Tier multiplies the base size. Jobs only declare these; Tiers and drawing are shared.
 */
public final class QuestType implements Identified {

    private final String id;
    private final Job job;
    private final int baseSize;

    public QuestType(String id, Job job, int baseSize) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Quest Type id must not be blank");
        }
        if (baseSize < 1) {
            throw new IllegalArgumentException("Base size must be 1 or more, got " + baseSize);
        }
        this.id = id;
        this.job = Objects.requireNonNull(job, "job");
        this.baseSize = baseSize;
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
