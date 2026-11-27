package com.alfgoto.rune.gameplay.woodcutting;

/** The effect ids of the Rhythm armor Traits. Values are percents, scaled by the rhythm Scale. */
public final class RhythmRules {

    /** Helmet: log breaking speed. */
    public static final String CHOP_SPEED = "rhythm_chop_speed";
    /** Chestplate: damage taken. */
    public static final String DAMAGE_REDUCTION = "rhythm_damage_reduction";
    /** Leggings: movement speed. */
    public static final String MOVE_SPEED = "rhythm_move_speed";
    /** Boots: jump strength. */
    public static final String JUMP = "rhythm_jump";
    /** Boots: fall damage taken. */
    public static final String FALL_REDUCTION = "rhythm_fall_reduction";

    private RhythmRules() {}

    public static boolean isRhythmEffect(String effectId) {
        return effectId.startsWith("rhythm_");
    }
}
